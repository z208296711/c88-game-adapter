package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.constants.LYFDConstants;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.FetchRecord;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGame;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.service.*;
import com.c88.game.adapter.service.third.v8.V8Encrypt;
import com.c88.game.adapter.service.third.vo.LYFDGameBetVO;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.c88.game.adapter.constants.AELotteryConstants.PLATFORM_CODE;

@SuppressWarnings("ALL")
@Slf4j
@Component
@RequiredArgsConstructor
public class LYFDGameAdapter implements IGameAdapter {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IFetchRecordService iFetchRecordService;

    private final KafkaMessageProducer kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final IPlatformService iPlatformService;

    private final IPlatformGameService iPlatformGameService;

    private final RestTemplate restTemplate;
    private final IRetryFetchOrder retryFetchOrderImpl;


    private String deskey;

    private String md5key;

    private String agent;

    private String apiUrl;

    private String recordUrl;

    private String prefix;

    @PostConstruct
    public void init() {
        Platform platform = iPlatformService.lambdaQuery()
                .eq(Platform::getCode, this.getGamePlatformCode())
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.agent = apiParameter.getApiId();
        this.deskey = apiParameter.getApiKey();
        this.md5key = apiParameter.getOthers();
        this.apiUrl = apiParameter.getApiUrl();
        this.prefix = apiParameter.getPrefix();
        this.recordUrl = apiParameter.getRecordUrl();
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {

        Date date = new Date();
        String paramStr = "s=0&account=" + this.getUsername(username) + "&money=0&orderid=" + generateOrderId(this.agent, this.getUsername(username), date) + "&ip=" + "35.187.204.224" + "&lineCode=1&KindID=" + param.get("GameId");
        try {
            JSONObject result = this.channelHandle(date, paramStr);
            JSONObject d = result.getJSONObject("d");
            int code = d.getInteger("code");
            if (code != 0) {
                return Result.failed();
            }
            String gameUrl = d.getString("url");
            gameUrl = gameUrl + "&backUrl=" + apiUrl.replaceAll("channelHandle", "") +
                    "&jumpType=" + 1 +
                    "&lang=" + "en-us";
            return Result.success(gameUrl);
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        //V8登入 會自動建立玩家資訊
        Result<String> result = this.login(username, Map.of("GameId", "510"));
        if (!Result.isSuccess(result)) {
            return Result.failed();
        }
        String platformUsername = this.getUsername(username);
        PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(this.getGamePlatformCode(), platformUsername);
        if (platformGameMember == null) {
            iPlatformGameMemberService.save(
                    PlatformGameMember.builder()
                            .memberId(memberId)
                            .username(platformUsername)
                            .platformId(platform.getId())
                            .code(this.getGamePlatformCode())
                            .build()
            );
        }
        return Result.success(username);

    }

    /**
     * 查询可下分
     */
    @Override
    public Result<BigDecimal> balance(String username) {
        Date date = new Date();
        String paramStr = "s=1&account=" + this.getUsername(username);
        try {
            JSONObject result = this.channelHandle(date, paramStr);
            JSONObject d = result.getJSONObject("d");
            int code = d.getInteger("code");
            if (code != 0) {
                Result.failed();
            }
            return Result.success(d.getBigDecimal("money"));
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @SneakyThrows
    public JSONObject getBetHistoryResult(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("request: getBetHistoryResult ,startTime={},endTime={}", startTime, endTime);

        String paramStr = "s=6&startTime=" + toServerTime(startTime) + "&endTime=" + toServerTime(endTime);
        return getRecordHandle(paramStr);
    }

    /**
     * @param agent
     * @param account
     * @param date
     * @return
     */
    private String generateOrderId(String agent, String account, Date date) {
        return agent + DateFormatUtils.formatUTC(date, "yyyyMMddHHmmssSSS") + account;
    }

    /**
     * channelHandle
     */
    private JSONObject channelHandle(Date date, String paramStr) throws Exception {
        String agent = this.agent;
        String deskey = this.deskey;
        String md5key = this.md5key;
        String commonApi = this.apiUrl;

        String value = V8Encrypt.AESEncrypt(paramStr, deskey);
        String key = V8Encrypt.MD5(agent + date.getTime() + md5key);
        String reqUrl = commonApi.concat("?agent=").concat(agent).concat("&timestamp=").concat("" + date.getTime()).concat("&param=").concat(value).concat("&key=").concat(key);

        log.info("channelHandle time: {}", date.getTime());
        log.info("channelHandle: paramStr: {}", paramStr);
        log.info("channelHandle: agent {}", agent);
        log.info("channelHandle: deskey {}", deskey);
        log.info("channelHandle: md5key {}", md5key);
        log.info("channelHandle: commonApi {}", commonApi);
        log.info("channelHandle: value {}", value);
        log.info("channelHandle: key {}", key);
        log.info("channelHandle: reqUrl {}", reqUrl);

        ResponseEntity<String> result = restTemplate.getForEntity(URI.create(reqUrl), String.class);
        JSONObject jsonObject = JSON.parseObject(result.getBody());
        log.info("channelHandle: result {}", jsonObject);
        JSONObject d = jsonObject.getJSONObject("d");
        String url = d.getString("url");
        url+="&ly_lang=en_us";
        d.put("url",url);
        jsonObject.put("d",d);
        return jsonObject;
    }

    /**
     * @param paramStr
     * @return
     * @throws Exception
     */
    private JSONObject getRecordHandle(String paramStr) throws Exception {
        log.info("getRecordHandle paramStr:{}", paramStr);
        Long time = System.currentTimeMillis();
        String agent = this.agent;
        String deskey = this.deskey;
        String md5key = this.md5key;
        String recordApi = this.recordUrl;
        String value = V8Encrypt.AESEncrypt(paramStr, deskey);
        String key = V8Encrypt.MD5(agent + time + md5key);
        String reqUrl = recordApi.concat("?agent=").concat(agent).concat("&timestamp=").concat("" + time).concat("&param=").concat(value).concat("&key=").concat(key);
        log.info("getRecordHandle:{}", reqUrl);
        ResponseEntity<String> result = restTemplate.getForEntity(URI.create(reqUrl), String.class);
        log.info("LYFD record result:{}", result.getBody());
        return JSON.parseObject(result.getBody());
    }

    @Override
    public String getUsername(String username) {
        return prefix + username;
    }

    @Override
    public String getGamePlatformCode() {
        return "LYFD";
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        Date date = new Date();
        String paramStr = "s=2&account=" + this.getUsername(username) + "&money=" + amount + "&orderid=" + generateOrderId(this.agent, this.getUsername(username), date);
        try {
            JSONObject result = this.channelHandle(date, paramStr);
            JSONObject d = result.getJSONObject("d");
            int code = d.getInteger("code");
            if (code != 0) {
                log.error("error:{}", d.getInteger("code"));
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.FAIL)
                                .build()
                );
            }

            return Result.success(
                    TransferStateVO.builder()
                            .balance(amount)
                            .state(AdapterTransferStateEnum.SUCCESS)
                            .build()
            );
        } catch (Exception e) {
            log.error("LYFD transferIn Exception : {}", ExceptionUtil.stacktraceToString(e));
            return Result.success(
                    TransferStateVO.builder()
                            .balance(amount)
                            .state(AdapterTransferStateEnum.UNKNOWN)
                            .build()
            );
        }

    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        Date date = new Date();
        String paramStr = "s=3&account=" + this.getUsername(username) + "&money=" + amount + "&orderid=" + generateOrderId(this.agent, this.getUsername(username), date);
        try {
            JSONObject result = this.channelHandle(date, paramStr);
            JSONObject d = result.getJSONObject("d");
            int code = d.getInteger("code");
            if (code != 0) {
                log.error("error:{}", d.getInteger("code"));
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.FAIL)
                                .build()
                );
            }

            return Result.success(
                    TransferStateVO.builder()
                            .balance(amount)
                            .state(AdapterTransferStateEnum.SUCCESS)
                            .build()
            );
        } catch (Exception e) {
            log.error("LYFD transferOut Exception : {}", ExceptionUtil.stacktraceToString(e));
            return Result.success(
                    TransferStateVO.builder()
                            .balance(amount)
                            .state(AdapterTransferStateEnum.UNKNOWN)
                            .build()
            );
        }
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        return null;
    }

    @Override
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        List<LYFDGameBetVO> list = this.fetchBetOrder(startDateTime, endDateTime);
        this.saveOrder(list);
    }

    @Override
    public void doFetchBetOrderAction() {
        FetchRecord fetchRecord = iFetchRecordService.getLastVersion(this.getGamePlatformCode());
        if (fetchRecord == null) {
            fetchRecord = new FetchRecord();
            fetchRecord.setPlatformCode(this.getGamePlatformCode());
            fetchRecord.setStartTime(LocalDateTime.now().minusMinutes(10));
        }
        LocalDateTime startTime = fetchRecord.getStartTime();
        LocalDateTime endTime = LocalDateTime.now();
        try {
            fetchRecord.setStartTime(endTime);
            List<LYFDGameBetVO> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("LYFD_exception:{}",ex.toString());
            FetchRecord errorBetOrder = new FetchRecord();
            errorBetOrder.setPlatformCode(this.getGamePlatformCode());
            errorBetOrder.setStartTime(startTime);
            errorBetOrder.setEndTime(endTime);
            errorBetOrder.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_ERROR.getValue());
            iFetchRecordService.saveBatch(Arrays.asList(fetchRecord, errorBetOrder));
            Object retryFetchOrder = retryFetchOrderImpl.retryFetchOrderTime(errorBetOrder);
            if (Objects.nonNull(retryFetchOrder)){
                errorBetOrder.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_DONE.getValue());
                iFetchRecordService.saveOrUpdate(errorBetOrder);
                if(CollectionUtils.isNotEmpty((List<LYFDGameBetVO>)retryFetchOrder)) {
                    this.saveOrder((List<LYFDGameBetVO>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    @Transactional
    public Long saveOrder(List<LYFDGameBetVO> list) {
        AtomicReference<Long> version = new AtomicReference<>(0L);
        Map<String, PlatformGame> gameMap = iPlatformGameService.getPlatformGameByPlatform(LYFDConstants.PLATFORM_CODE).stream().collect(Collectors.toMap(PlatformGame::getGameId, Function.identity()));
        list.forEach(vo -> {

            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(this.getGamePlatformCode(), vo.getAccount());
            if (platformGameMember == null) {
                return;
            }

            BigDecimal winLoss = vo.getProfit();
            BigDecimal validBet = vo.getValidBet();
            BigDecimal settle = vo.getProfit().add(vo.getValidBet());
            // 派彩= 輸贏 + 投注
            Integer status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getTransactionId());
            betOrder.setTransactionSerial(vo.getTransactionId());
            betOrder.setTransactionTime(vo.getGameStartTime());

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

//            PlatformGame platformGame = iPlatformGameService.getPlatformGameByCode(this.getGamePlatformCode(), String.valueOf(vo.getGameId()));
//            if (platformGame == null) {
//                log.error("platformGame:{} is null", vo.getGameId());
//                return;
//            }

            betOrder.setGameId(String.valueOf(vo.getGameId()));
            PlatformGame platformGame = gameMap.get(String.valueOf(vo.getGameId()));
            betOrder.setGameName(platformGame==null ? LYFDConstants.UNKNOWN : gameMap.get(String.valueOf(vo.getGameId())).getNameEn());
            betOrder.setPlatformCode(this.getGamePlatformCode());
            betOrder.setGameCategoryCode("slot");
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(validBet);
            betOrder.setSettle(settle);
            betOrder.setSettleTime(vo.getGameEndTime());
            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(this.getGamePlatformCode());

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

        });
        return version.get();
    }

    @Override
    public List<LYFDGameBetVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        JSONObject jsonResult = this.getBetHistoryResult(startTime, endTime);
        JSONObject d = jsonResult.getJSONObject("d");
        int code = d.getInteger("code");
        if (code != 0) {
            return Collections.emptyList();
        }
        int count = d.getInteger("count");
        JSONObject result = d.getJSONObject("list");
        log.info("LYFD result:{}", result);
        JSONArray kindArray = result.getJSONArray("KindID");
        JSONArray profitArray = result.getJSONArray("Profit");
        JSONArray accountArray = result.getJSONArray("Accounts");
        JSONArray allBetArray = result.getJSONArray("AllBet");
        JSONArray revenueArray = result.getJSONArray("Revenue");
        JSONArray cellScoreArray = result.getJSONArray("CellScore");
        JSONArray gameIdArray = result.getJSONArray("GameID");
        JSONArray gameStartTimeArray = result.getJSONArray("GameStartTime");
        JSONArray gameEndTimeArray = result.getJSONArray("GameEndTime");
        List<LYFDGameBetVO> voList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Integer kindId = kindArray.getInteger(i);
            BigDecimal profit = profitArray.getBigDecimal(i);
            String account = accountArray.getString(i);
            String gameId = gameIdArray.getString(i);
            BigDecimal betAmount = allBetArray.getBigDecimal(i);
            BigDecimal revenue = revenueArray.getBigDecimal(i);
            String gameStartTime = gameStartTimeArray.getString(i);
            String gameEndTime = gameEndTimeArray.getString(i);
            BigDecimal validBet = cellScoreArray.getBigDecimal(i);

            LYFDGameBetVO vo = new LYFDGameBetVO();
            vo.setTransactionId(gameId);
            vo.setAccount(account.split("_")[1]);
            vo.setGameId(kindId);
            vo.setProfit(profit);
            vo.setRevenue(revenue);
            vo.setValidBet(validBet);
            vo.setAllBet(betAmount);
            //LocalDateTime.ofInstant(Instant.parse(ae.getBetTime()), ZoneOffset.UTC)
            LocalDateTime start = LocalDateTime.parse(gameStartTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(gameEndTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            vo.setGameStartTime(start.atZone(ZoneId.of("Asia/Shanghai")).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime());
            vo.setGameEndTime(end.atZone(ZoneId.of("Asia/Shanghai")).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime());
            voList.add(vo);
        }
        return voList;
    }

    private String getOriginUsername(String username) {
        return username.substring(3);
    }

    public static Long toServerTime(LocalDateTime localDateTime){
        ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.ofHours(8));
        return converted.toInstant().toEpochMilli();
    }
}
