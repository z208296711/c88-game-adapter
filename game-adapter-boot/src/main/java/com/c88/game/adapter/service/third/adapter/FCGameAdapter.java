package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.DateUtil;
import com.c88.game.adapter.constants.FCConstants;
import com.c88.game.adapter.constants.JILIConstants;
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
import com.c88.game.adapter.service.third.vo.FCGameVO;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.c88.game.adapter.constants.AELotteryConstants.PLATFORM_CODE;
import static com.c88.game.adapter.constants.FCConstants.BET_RECORD;
import static com.c88.game.adapter.constants.FCConstants.CHECK_STATUS;

@SuppressWarnings("ALL")
@Slf4j
@Component
@RequiredArgsConstructor
public class FCGameAdapter implements IGameAdapter {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IFetchRecordService iFetchRecordService;

    private final KafkaMessageProducer kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final IPlatformService iPlatformService;

    private final IPlatformGameService iPlatformGameService;

    private final RestTemplate restTemplate;

    private final IPlatformGameService gameService;
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

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MemberAccount", this.getUsername(username));
        jsonObject.put("GameID", param.get("GameId"));
        jsonObject.put("LoginGameHall", "");

        try {
            JSONObject response = this.channelHandle(FCConstants.LOGIN, jsonObject);

            if (response.getInteger("Result") != 0) {
                log.info("FC register fail : {}", response);
                return Result.failed(response.getInteger("Result").toString());
            }
            return Result.success(response.getString("Url"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        //V8登入 會自動建立玩家資訊
        Result<String> result = this.login(username, Map.of("GameId", "22016"));
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
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(this.getUsername(username));
        jsonObject.put("MemberList", jsonArray);

        try {
            JSONObject result = this.channelHandle(FCConstants.BALANCE, jsonObject);
            log.info("FC balance:{}", result);
            if (result.getInteger("Result") != 0) {
                Result.failed();
            }

            return Result.success(result.getJSONArray("MemberList").getJSONObject(0).getBigDecimal(this.getUsername(username)));
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @SneakyThrows
    public JSONObject getBetHistoryResult(LocalDateTime startTime, LocalDateTime endTime) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("StartDate", DateUtil.dateToStr(startTime, "yyyy-MM-dd HH:mm:ss"));
        jsonObject.put("EndDate", DateUtil.dateToStr(endTime, "yyyy-MM-dd HH:mm:ss"));

        return getRecordHandle(FCConstants.BET_RECORD_HISTORY, jsonObject);
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
    private JSONObject channelHandle(String subUrl, JSONObject jsonObject) throws Exception {
        String agent = this.agent;
        String deskey = this.deskey;
        String md5key = this.md5key;
        String commonApi = this.apiUrl;

        String url = commonApi + subUrl;

        log.info("FC original json:{}", jsonObject);
        String params = AESEncrypt(JSON.toJSONString(jsonObject), deskey);
        String sign = V8Encrypt.MD5(JSON.toJSONString(jsonObject));
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        //接口参数
        map.add("AgentCode", agent);
        map.add("Currency", "PHP");
        map.add("Params", params);
        map.add("Sign", sign);

        //头部类型
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        //构造实体对象
        HttpEntity<MultiValueMap<String, Object>> toParam = new HttpEntity<>(map, headers);

        log.info("FC api url: {}, params : {}", url, toParam);
        //发起请求,服务地址，请求参数，返回消息体的数据类型
        JSONObject response = restTemplate.postForObject(url, toParam, JSONObject.class);
        log.info("FC original response:{}", response);
        return response;
    }

    /**
     * @param paramStr
     * @return
     * @throws Exception
     */
    private JSONObject getRecordHandle(String suburl, JSONObject jsonObject) throws Exception {
        log.info("getRecordHandle paramStr:{}", jsonObject);

        JSONObject result = this.channelHandle(suburl, jsonObject);

        return result;
    }

    @Override
    public String getUsername(String username) {
        return prefix + username;
    }

    @Override
    public String getGamePlatformCode() {
        return "FC";
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MemberAccount", this.getUsername(username));
        jsonObject.put("TrsID", generateOrderId(this.agent, username, new Date()).replaceAll("-", ""));
        jsonObject.put("AllOut", 0);
        jsonObject.put("Points", amount.setScale(2, RoundingMode.HALF_UP));

        try {
            JSONObject response = this.channelHandle(FCConstants.SET_POINTS, jsonObject);

            if (response.getInteger("Result") != 0) {
                log.error("FC transfer in fail : {}", response);
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.FAIL)
                                .build()
                );
            }

            return Result.success(
                    TransferStateVO.builder()
                            .balance(response.getBigDecimal("Points"))
                            .state(AdapterTransferStateEnum.SUCCESS)
                            .build()
            );

        } catch (Exception e) {
            log.error("FC transferIn Exception : {}", ExceptionUtil.stacktraceToString(e));
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MemberAccount", this.getUsername(username));
        jsonObject.put("TrsID", generateOrderId(this.agent, username, new Date()).replaceAll("-", ""));
        jsonObject.put("AllOut", 1);

        try {
            JSONObject response = this.channelHandle(FCConstants.SET_POINTS, jsonObject);

            if (response.getInteger("Result") != 0) {
                log.info("FC transfer out fail : {}", response);
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.FAIL)
                                .build()
                );
            }

            return Result.success(
                    TransferStateVO.builder()
                            .balance(response.getBigDecimal("Points").abs())
                            .state(AdapterTransferStateEnum.SUCCESS)
                            .build()
            );
        } catch (Exception e) {
            log.error("FC transferOut Exception : {}", ExceptionUtil.stacktraceToString(e));
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("TrsID", orderId);
        try {
            JSONObject response = this.channelHandle(CHECK_STATUS, jsonObject);
            if (response.getInteger("Result") != 0) {
                log.info("FC transfer out fail : {}", response);
                return Result.failed(response.getInteger("Result").toString());
            }
            if (response.getInteger("status") == 1) {
                return Result.success("TRANSFER_SUCCESS");
            }
        } catch (Exception e) {
            return Result.success("TRANSFER_FAIL");
        }
        return Result.success("TRANSFER_NOT_FOUND");
    }

    @Override
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        List<FCGameVO> list = this.fetchBetOrder(startDateTime, endDateTime);
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
            List<FCGameVO> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("FC_exception:{}",ex.toString());
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
                if(CollectionUtils.isNotEmpty((List<FCGameVO>)retryFetchOrder)) {
                    this.saveOrder((List<FCGameVO>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    @Transactional
    public Long saveOrder(List<FCGameVO> list) {
        AtomicReference<Long> version = new AtomicReference<>(0L);
        Map<String, PlatformGame> gameMap = gameService.getPlatformGameByPlatform(FCConstants.PLATFORM_CODE).stream().collect(Collectors.toMap(PlatformGame::getGameId, Function.identity()));
        list.forEach(vo -> {

            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(this.getGamePlatformCode(), vo.getAccount());
            if (platformGameMember == null) {
                return;
            }

            BigDecimal winLoss = new BigDecimal(vo.getWinlose());
            BigDecimal validBet = new BigDecimal(vo.getBet());
            BigDecimal settle = new BigDecimal(vo.getPrize());
            // 派彩= 輸贏 + 投注
            Integer status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getRecordID());
            betOrder.setTransactionSerial(vo.getRecordID());
            betOrder.setTransactionTime(toLocalServerTime(vo.getBdate()));

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

//            PlatformGame platformGame = iPlatformGameService.getPlatformGameByCode(this.getGamePlatformCode(), String.valueOf(vo.getGameID()));
//            if (platformGame == null) {
//                log.error("platformGame:{} is null", vo.getGameID());
//                return;
//            }
            betOrder.setGameId(String.valueOf(vo.getGameID()));
            betOrder.setGameName( gameMap.get(String.valueOf(vo.getGameID())).getNameEn());
            betOrder.setPlatformCode(this.getGamePlatformCode());
            //1 捕鱼机 2 老虎机
            betOrder.setGameCategoryCode(vo.getGametype() == 1 ? "special" : "slot");
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(validBet);
            betOrder.setSettle(settle);
            betOrder.setSettleTime(toLocalServerTime(vo.getBdate()));
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
    public List<FCGameVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("StartDate", DateUtil.dateToStr(toServerTime(startTime), "yyyy-MM-dd HH:mm:ss"));
        jsonObject.put("EndDate", DateUtil.dateToStr(toServerTime(endTime), "yyyy-MM-dd HH:mm:ss"));

        JSONObject result = null;
        try {
            result = getRecordHandle(BET_RECORD, jsonObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (result.getInteger("Result") != 0) {
            return Collections.emptyList();
        }

        return result.getJSONArray("Records").toJavaList(FCGameVO.class);
    }

    private String getOriginUsername(String username) {
        return username.substring(3);
    }

    private static String AESEncrypt(String value, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] raw = key.getBytes("UTF-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static LocalDateTime toServerTime(LocalDateTime localDateTime) {
        ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.ofHours(-4));
//log.info("ori:{}, zone:{}, convert:{}", localDateTime, zonedtime, converted);
        return converted.toLocalDateTime();
    }

    private static LocalDateTime toLocalServerTime(LocalDateTime localDateTime) {
        ZonedDateTime zonedtime = localDateTime.atZone(ZoneOffset.ofHours(-4));
        ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneId.systemDefault());
        return converted.toLocalDateTime();
    }

}
