package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.constants.AELotteryConstants;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.vo.AELotteryBetOrderListVO;
import com.c88.game.adapter.service.third.vo.AELotteryBetOrderVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.c88.game.adapter.constants.AELotteryConstants.BALANCE_URL;
import static com.c88.game.adapter.constants.AELotteryConstants.GAME_CATEGORY_CODE;
import static com.c88.game.adapter.constants.AELotteryConstants.LOGIN_URL;
import static com.c88.game.adapter.constants.AELotteryConstants.PLATFORM_CODE;
import static com.c88.game.adapter.constants.AELotteryConstants.REGISTER_URL;
import static com.c88.game.adapter.constants.AELotteryConstants.RESPONSE_SUCCESS;
import static com.c88.game.adapter.constants.AELotteryConstants.TURN_IN_URL;
import static com.c88.game.adapter.constants.AELotteryConstants.TURN_OUT_URL;
import static com.c88.game.adapter.constants.AELotteryConstants.TURN_STATUS_URL;
import static com.c88.game.adapter.constants.AELotteryConstants.USERNAME_EXISTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class AELotteryGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private String prefix;
    private String apiId;
    private String apiKey;
    private String apiUrl;
    private String recordUrl;
    private String locale;
    private Integer enable;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.apiId = apiParameter.getApiId();
        this.apiKey = apiParameter.getApiKey();
        this.apiUrl = apiParameter.getApiUrl();
        this.recordUrl = apiParameter.getRecordUrl();
        this.prefix = apiParameter.getPrefix();
        this.locale = apiParameter.getLocale();
        this.enable = platform.getEnable();
    }

    @Override
    public String getUsername(String username) {
        return prefix + username;
    }

    @Override
    public String getGamePlatformCode() {
        return AELotteryConstants.PLATFORM_CODE;
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {
        String url = apiUrl + LOGIN_URL;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));
        jsonObject.put("locale", locale);// 選擇語系 default=en-us
        jsonObject.put("is_mobile", Boolean.FALSE);// 選擇使用手機版URL default=false

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject);

        log.info("AE lottery before login url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        log.info("AE lottery login jsonResult:{}", jsonResult);
        JSONObject jsonStatus = jsonResult.getJSONObject("status");
        JSONObject data = jsonResult.getJSONObject("data");
        String resultCode = jsonStatus.getString("code");

        if (!AELotteryConstants.RESPONSE_SUCCESS.equals(resultCode)) {
            return Result.failed(jsonStatus.getString("message"));
        }
        return Result.success(data.getString("url"));
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        String url = apiUrl + REGISTER_URL;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject);

        log.info("AE register url: {}, entity : {}", url, JSON.toJSONString(entity));
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        JSONObject jsonStatus = jsonResult.getJSONObject("status");
        String resultCode = jsonStatus.getString("code");

        if (!List.of(USERNAME_EXISTS, RESPONSE_SUCCESS).contains(resultCode)) {
            return Result.failed(jsonStatus.getString("message"));
        }

        // 寫入新帳號
        if (RESPONSE_SUCCESS.equals(resultCode)) {
            iPlatformGameMemberService.save(
                    PlatformGameMember.builder()
                            .memberId(memberId)
                            .username(this.getUsername(username))
                            .platformId(platform.getId())
                            .code(AELotteryConstants.PLATFORM_CODE)
                            .build()
            );
        }

        return Result.success(this.getUsername(username));
    }

    @Override
    public Result<BigDecimal> balance(String username) {

        String url = apiUrl + BALANCE_URL;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject);

        log.info("AE balance url: {}, jsonObject : {}", url, JSON.toJSONString(entity));

        JSONObject jsonResult;
        try {
            jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        } catch (Exception e) {
            log.info("AE Lottery balance Exception : {}", ExceptionUtil.getRootCauseMessage(e));
            return Result.success(BigDecimal.ZERO);
        }
        log.info("AE balance : {},", jsonResult);
        JSONObject jsonStatus = jsonResult.getJSONObject("status");
        JSONObject jsonData = jsonResult.getJSONObject("data");
        String resultCode = jsonStatus.getString("code");

        if (!RESPONSE_SUCCESS.equals(resultCode)) {
            return Result.failed(jsonStatus.getString("message"));
        }

        return Result.success(jsonData.getBigDecimal("balance"));
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        String url = apiUrl + TURN_IN_URL;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));
        jsonObject.put("transaction_id", transactionNo);
        jsonObject.put("amount", amount);

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject);

        log.info("AE transfer in url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        JSONObject jsonStatus = jsonResult.getJSONObject("status");
        JSONObject jsonData = jsonResult.getJSONObject("data");
        String resultCode = jsonStatus.getString("code");

        if (!RESPONSE_SUCCESS.equals(resultCode)) {
            return Result.failed(jsonStatus.getString("message"));
        }

        TransferStateVO stateVO = new TransferStateVO();
        stateVO.setBalance(jsonData.getBigDecimal("balance"));
        stateVO.setState(AdapterTransferStateEnum.SUCCESS);
        return Result.success(stateVO);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        String url = apiUrl + TURN_OUT_URL;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));
        jsonObject.put("transaction_id", transactionNo);
        jsonObject.put("amount", amount.negate());

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject);

        log.info("AE transfer in url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        JSONObject jsonStatus = jsonResult.getJSONObject("status");
        JSONObject jsonData = jsonResult.getJSONObject("data");
        String resultCode = jsonStatus.getString("code");

        if (!RESPONSE_SUCCESS.equals(resultCode)) {
            return Result.failed(jsonStatus.getString("message"));
        }

        TransferStateVO stateVO = new TransferStateVO();
        stateVO.setBalance(jsonData.getBigDecimal("balance"));
        stateVO.setState(AdapterTransferStateEnum.SUCCESS);
        return Result.success(stateVO);
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        String url = apiUrl + TURN_STATUS_URL;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transaction_id", orderId);

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject);

        log.info("AE findTicketStatus url: {}, jsonObject : {}", url, JSON.toJSONString(entity));

        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        JSONObject jsonStatus = jsonResult.getJSONObject("status");
        JSONObject jsonData = jsonResult.getJSONObject("data");
        String resultCode = jsonStatus.getString("code");

        if (!RESPONSE_SUCCESS.equals(resultCode)) {
            return Result.failed(jsonStatus.getString("message"));
        }

        return Result.success(jsonData.toJSONString());
    }

    @Override
    public void manualBetOrder(LocalDateTime startTime, LocalDateTime endTime, Map<String, String> param) {
        Result<AELotteryBetOrderVO> aeLotteryBertOrderVOResult = this.fetchBetOrder(startTime, endTime, 1, 1000);
        if (Result.isSuccess(aeLotteryBertOrderVOResult)) {
            this.saveOrder(aeLotteryBertOrderVOResult.getData().getList());
        }
    }

    @Override
    public void doFetchBetOrderAction() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = LocalDateTime.now();

        int page = 1;
        int pageSize = 1000;
        Result<AELotteryBetOrderVO> aeLotteryBertOrderVOResult = this.fetchBetOrder(startTime, endTime, page, pageSize);

        if (Result.isFail(aeLotteryBertOrderVOResult)) {
            return;
        }

        AELotteryBetOrderVO data = aeLotteryBertOrderVOResult.getData();
        List<AELotteryBetOrderListVO> datasource = data.getList();
        for (int x = pageSize; x < data.getTotalRecord(); x = x + pageSize) {
            Result<AELotteryBetOrderVO> result = this.fetchBetOrder(startTime, endTime, page, pageSize);
            if (Result.isFail(result)) {
                break;
            }
            datasource.addAll(result.getData().getList());
        }

        this.saveOrder(datasource);
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    @Override
    public List<AELotteryBetOrderListVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        int page = 1;
        int pageSize = 1000;

        Result<AELotteryBetOrderVO> aeLotteryBertOrderVOResult = this.fetchBetOrder(startTime, endTime, page, pageSize);

        if (Result.isFail(aeLotteryBertOrderVOResult)) {
            return Collections.emptyList();
        }

        AELotteryBetOrderVO data = aeLotteryBertOrderVOResult.getData();
        List<AELotteryBetOrderListVO> datasource = data.getList();
        for (int x = pageSize; x < data.getTotalRecord(); x = x + pageSize) {
            Result<AELotteryBetOrderVO> result = this.fetchBetOrder(startTime, endTime, page, pageSize);
            if (Result.isFail(result)) {
                break;
            }
            datasource.addAll(result.getData().getList());
        }

        return datasource;
    }

    private Result<AELotteryBetOrderVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("start_time", startTime.format(dtf));
        jsonObject.put("end_time", endTime.format(dtf));
        jsonObject.put("page", page);
        jsonObject.put("per_page", pageSize);

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject);

        log.info("AE fetchBetOrder url: {}, jsonObject : {}", recordUrl, JSON.toJSONString(jsonObject));

        JSONObject jsonResult = restTemplate.postForObject(recordUrl, entity, JSONObject.class);
        JSONObject jsonStatus = jsonResult.getJSONObject("status");
        String resultCode = jsonStatus.getString("code");

        if (!RESPONSE_SUCCESS.equals(resultCode)) {
            return Result.failed(jsonStatus.getString("message"));
        }

        return Result.success(jsonResult.getObject("data", AELotteryBetOrderVO.class));
    }

    @Transactional
    public boolean saveOrder(List<AELotteryBetOrderListVO> datasource) {
        datasource.parallelStream().forEach(ae ->
                {
                    PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(
                            PLATFORM_CODE, ae.getAccount());
                    if (Objects.isNull(platformGameMember)) {
                        return;
                    }

                    BetOrderVO betOrder = BetOrderVO.builder()
                            .transactionNo(ae.getOrderId())
                            .transactionSerial(ae.getOrderId())
                            .transactionTime(LocalDateTime.ofEpochSecond(ae.getBetAt(), 0, ZoneOffset.UTC))
                            .memberId(platformGameMember.getMemberId())
                            .username(platformGameMember.getUsername().replaceAll(prefix, ""))
                            .platformId(platformGameMember.getPlatformId())
                            .gameId(ae.getGameId())
                            .platformCode(PLATFORM_CODE)
                            .gameName(ae.getWagerBetItem())
                            .gameCategoryCode(GAME_CATEGORY_CODE)
                            .validBetAmount(ae.getValidBet())
                            .betAmount(ae.getBetAmount())
                            .settle(ae.getPaidAmount())
                            .settleTime(LocalDateTime.ofEpochSecond(ae.getPaidAt(), 0, ZoneOffset.UTC))
                            .winLoss(ae.getPlayerProfitAmount())
                            .betState(ae.getStatus() == 1 ? BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue() : BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())
                            .rebateState(ae.getStatus() == 1 ? ReBateStatusEnum.UN_SETTLED.getValue() : ReBateStatusEnum.SETTLED.getValue())
                            .build();

                    //發送Mq
                    kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);
                }
        );

        return Boolean.TRUE;
    }

    private HttpEntity<JSONObject> generateHttpEntity(JSONObject jsonObject) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("BC", apiId);
        httpHeaders.add("Authorization", apiKey);
        httpHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        return new HttpEntity<>(jsonObject, httpHeaders);
    }

}
