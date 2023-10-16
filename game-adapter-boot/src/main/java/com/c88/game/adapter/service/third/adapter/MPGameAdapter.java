package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.constants.LYFDConstants;
import com.c88.game.adapter.constants.MPConstants;
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
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.google.common.base.Joiner;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MPGameAdapter implements IGameAdapter {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IFetchRecordService iFetchRecordService;

    private final KafkaMessageProducer kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final IPlatformService iPlatformService;

    private final IPlatformGameService iPlatformGameService;

    private final IRetryFetchOrder retryFetchOrderImpl;

    private final RestTemplate restTemplate;

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
                .orElse(Platform.builder().code(MPConstants.PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());
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
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("s", 0);
        paramMap.put("account", getUsername(username));
        paramMap.put("money", 0);
        paramMap.put("orderid", generateOrderId(agent, getUsername(username), date));
        paramMap.put("ip", "");
        paramMap.put("lineCode", 1);
        paramMap.put("KindID", param.get("GameId"));
        String paramStr = Joiner.on("&").withKeyValueSeparator("=").join(paramMap);
        try {
            JSONObject result = this.channelHandle(date, paramStr);
            JSONObject d = result.getJSONObject("d");
            int code = d.getInteger("code");
            if (code != 0) {
                return Result.failed();
            }
            String gameUrl = d.getString("url");
            gameUrl = gameUrl + "&returnUrl=" + apiUrl.replaceAll("channelHandle", "") +
                    "&returnType=" + 2 +
                    "&lang=" + "en-us";

            return Result.success(gameUrl);
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        Result<String> result = this.login(username, Map.of("GameId", "0"));
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
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("s", 7);
        paramMap.put("account", getUsername(username));
        String paramStr = Joiner.on("&").withKeyValueSeparator("=").join(paramMap);

        try {
            JSONObject result = this.channelHandle(date, paramStr);
            JSONObject d = result.getJSONObject("d");
            int code = d.getInteger("code");
            if (code != 0) {
                Result.failed();
            }
            return Result.success(d.getBigDecimal("freeMoney"));
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @SneakyThrows
    public JSONObject getBetHistoryResult(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("request: getBetHistoryResult ,startTime={},endTime={}", startTime, endTime);
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("s", 6);
        paramMap.put("startTime", startTime.atZone(ZoneId.from(ZoneOffset.UTC)).toInstant().toEpochMilli());
        paramMap.put("endTime", endTime.atZone(ZoneId.from(ZoneOffset.UTC)).toInstant().toEpochMilli());
        String paramStr = Joiner.on("&").withKeyValueSeparator("=").join(paramMap);

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
        String param = V8Encrypt.AESEncrypt(paramStr, deskey);
        String key = V8Encrypt.MD5(agent + date.getTime() + md5key);
        String reqUrl = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("agent", agent)
                .queryParam("timestamp", date.getTime())
                .queryParam("param", param)
                .queryParam("key", key).build().toString();

        log.info("channelHandle time: {}", date.getTime());
        log.info("channelHandle: paramStr: {}", paramStr);
        log.info("channelHandle: agent {}", agent);
        log.info("channelHandle: deskey {}", deskey);
        log.info("channelHandle: md5key {}", md5key);
        log.info("channelHandle: commonApi {}", reqUrl);
        log.info("channelHandle: value {}", param);
        log.info("channelHandle: key {}", key);

        ResponseEntity<String> result = restTemplate.getForEntity(URI.create(reqUrl), String.class);
        return JSON.parseObject(result.getBody());
    }

    /**
     * @param paramStr
     * @return
     * @throws Exception
     */
    private JSONObject getRecordHandle(String paramStr) throws Exception {
        log.info("getRecordHandle paramStr:{}", paramStr);
        Long time = toUtcBeijing(LocalDateTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String encrypt = V8Encrypt.AESEncrypt(paramStr, deskey);
        String key = V8Encrypt.MD5(agent + time + md5key);
        String reqUrl = UriComponentsBuilder.fromUriString(recordUrl)
                .queryParam("agent", agent)
                .queryParam("timestamp", time)
                .queryParam("param", encrypt)
                .queryParam("key", key).build().toString();

        log.info("getRecordHandle:{}", reqUrl);
        ResponseEntity<String> result = restTemplate.getForEntity(URI.create(reqUrl), String.class);
        return JSON.parseObject(result.getBody());
    }

    @Override
    public String getUsername(String username) {
        return prefix + username;
    }

    @Override
    public String getGamePlatformCode() {
        return "MP";
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        Date date = new Date();
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("s", 2);
        paramMap.put("account", getUsername(username));
        paramMap.put("money", amount);
        paramMap.put("orderid", generateOrderId(this.agent, this.getUsername(username), date));
        String paramStr = Joiner.on("&").withKeyValueSeparator("=").join(paramMap);

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
            log.error("MP transferIn Exception : {}", ExceptionUtil.stacktraceToString(e));
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
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("s", 3);
        paramMap.put("account", getUsername(username));
        paramMap.put("money", amount);
        paramMap.put("orderid", generateOrderId(this.agent, this.getUsername(username), date));
        String paramStr = Joiner.on("&").withKeyValueSeparator("=").join(paramMap);
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
            log.error("MP transferOut Exception : {}", ExceptionUtil.stacktraceToString(e));
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
        Date date = new Date();
        log.info("request: findTicketStatus ,username={},orderId={}", username, orderId);
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("s", 4);
        paramMap.put("orderid", orderId);
        String paramStr = Joiner.on("&").withKeyValueSeparator("=").join(paramMap);
        try {
            JSONObject jsonObject = this.channelHandle(date, paramStr);
            JSONObject d = jsonObject.getJSONObject("d");
            String msg = d.getString("status");
            switch (msg) {
                case "3":
                    return Result.success("TRANSFER_PROCESSING");
                case "0":
                    return Result.success("TRANSFER_SUCCESS");
                case "2":
                    return Result.success("TRANSFER_FAIL");
                default:
                    Result.failed("not exist");
                    break;
            }
            return Result.success("TRANSFER_PROCESSING");
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }

    }

    @Override
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        List<MPGameBetVO> list = this.fetchBetOrder(startDateTime, endDateTime);
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
                List<MPGameBetVO> list = this.fetchBetOrder(startTime, endTime);
                if (CollectionUtils.isNotEmpty(list)) {
                    this.saveOrder(list);
                }
                iFetchRecordService.saveOrUpdate(fetchRecord);
            } catch (Exception ex) {
                XxlJobHelper.log("MP_exception:{}",ex.toString());
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
                    if(CollectionUtils.isNotEmpty((List<MPGameBetVO>)retryFetchOrder)) {
                        this.saveOrder((List<MPGameBetVO>)retryFetchOrder);
                    }
                }
            }

    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    @Transactional
    public Long saveOrder(List<MPGameBetVO> list) {
        AtomicReference<Long> version = new AtomicReference<>(0L);
        Map<String, PlatformGame> gameMap = iPlatformGameService.getPlatformGameByPlatform(MPConstants.PLATFORM_CODE).stream().collect(Collectors.toMap(PlatformGame::getGameId, Function.identity()));
        list.forEach(vo -> {
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(this.getGamePlatformCode(), vo.getAccount());
            if (platformGameMember == null) {
                return;
            }

            BigDecimal winLoss = vo.getProfit();
            BigDecimal validBet = vo.getCellScore();
            BigDecimal settle = vo.getProfit().add(vo.getCellScore());
            // 派彩= 輸贏 + 投注
            Integer status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getGameId() + vo.getTableId() + vo.getChairId());
            betOrder.setTransactionSerial(vo.getGameId() + vo.getTableId() + vo.getChairId());
            betOrder.setTransactionTime(vo.getGameStartTime());

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());
            Integer gameId= vo.getKindId();
            String name = "未接入之遊戲";
            if(gameMap.containsKey(String.valueOf(vo.getKindId()))){
                name = gameMap.get(String.valueOf(vo.getKindId())).getNameEn();
            }
            betOrder.setGameId(String.valueOf(gameId));
            betOrder.setGameName(name);
            betOrder.setPlatformCode(this.getGamePlatformCode());
            betOrder.setGameCategoryCode("chess");
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
    public List<MPGameBetVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        JSONObject jsonResult = this.getBetHistoryResult(startTime, endTime);
        log.info("getBetHistoryResult:{}", jsonResult.toString());
        JSONObject d = jsonResult.getJSONObject("d");
        int code = d.getInteger("code");
        if (code != 0) {
            return Collections.emptyList();
        }
        int count = d.getInteger("count");
        JSONObject result = d.getJSONObject("list");
        log.info("MP result:{}", result);
        JSONArray gameid = result.getJSONArray("GameID");
        JSONArray accounts = result.getJSONArray("Accounts");
        JSONArray serverid = result.getJSONArray("ServerID");
        JSONArray kindid = result.getJSONArray("KindID");
        JSONArray tableid = result.getJSONArray("TableID");
        JSONArray chairid = result.getJSONArray("ChairID");
        JSONArray userCount = result.getJSONArray("UserCount");
        JSONArray cardValue = result.getJSONArray("CardValue");
        JSONArray cellscore = result.getJSONArray("CellScore");
        JSONArray allbet = result.getJSONArray("AllBet");
        JSONArray profit = result.getJSONArray("Profit");
        JSONArray revenue = result.getJSONArray("Revenue");
        JSONArray newScore = result.getJSONArray("NewScore");
        JSONArray gameStartTime = result.getJSONArray("GameStartTime");
        JSONArray gameEndTime = result.getJSONArray("GameEndTime");
        JSONArray channelId = result.getJSONArray("ChannelID");
        JSONArray lineCode = result.getJSONArray("LineCode");
        List<MPGameBetVO> voList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MPGameBetVO vo = MPGameBetVO.builder()
                    .gameId(gameid.getString(i))
                    .account(accounts.getString(i).substring(accounts.getString(i).indexOf("_") + 1))
                    .serverId(serverid.getInteger(i))
                    .kindId(kindid.getInteger(i))
                    .tableId(tableid.getInteger(i))
                    .chairId(chairid.getInteger(i))
                    .userCount(userCount.getInteger(i))
                    .cardValue(cardValue.getString(i))
                    .cellScore(cellscore.getBigDecimal(i))
                    .allBet(allbet.getBigDecimal(i))
                    .profit(profit.getBigDecimal(i))
                    .revenue(revenue.getBigDecimal(i))
                    .newScore(newScore.getBigDecimal(i))
                    .gameStartTime(LocalDateTime.parse(gameStartTime.getString(i), dateTimeFormatter).atZone(ZoneId.of("+8")).withZoneSameInstant(ZoneId.of("Etc/GMT+0")).toLocalDateTime())
                    .gameEndTime(LocalDateTime.parse(gameEndTime.getString(i), dateTimeFormatter).atZone(ZoneId.of("+8")).withZoneSameInstant(ZoneId.of("Etc/GMT+0")).toLocalDateTime())
                    .channelId(channelId.getInteger(i))
                    .lineCode(lineCode.getString(i)).build();
            voList.add(vo);
        }
        return voList;
    }

    private String getOriginUsername(String username) {
        return username.substring(3);
    }

    public static LocalDateTime toUtcBeijing(LocalDateTime localDateTime) {
        ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.from(ZoneOffset.UTC));
        ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.ofHours(8));
        return converted.toLocalDateTime();
    }


}
