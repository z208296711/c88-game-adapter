package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.constants.AELiveConstants;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.vo.AELiveBetOrderListVO;
import com.c88.game.adapter.service.third.vo.AELiveTransactionVO;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.c88.game.adapter.constants.AELiveConstants.ACCOUNT_EXIST;
import static com.c88.game.adapter.constants.AELiveConstants.BALANCE_BY_USER;
import static com.c88.game.adapter.constants.AELiveConstants.BALANCE_URL;
import static com.c88.game.adapter.constants.AELiveConstants.BET_RECORD_URL_BY_TIME;
import static com.c88.game.adapter.constants.AELiveConstants.CURRENCY;
import static com.c88.game.adapter.constants.AELiveConstants.GAME_CODE;
import static com.c88.game.adapter.constants.AELiveConstants.GAME_TYPE;
import static com.c88.game.adapter.constants.AELiveConstants.LANG_VI;
import static com.c88.game.adapter.constants.AELiveConstants.LIVE_BET_LIMIT_ID;
import static com.c88.game.adapter.constants.AELiveConstants.LOGIN_URL;
import static com.c88.game.adapter.constants.AELiveConstants.PLATFORM_CODE;
import static com.c88.game.adapter.constants.AELiveConstants.PLATFORM_LIVE_SEXYBCRT;
import static com.c88.game.adapter.constants.AELiveConstants.REGISTER_URL;
import static com.c88.game.adapter.constants.AELiveConstants.RESPONSE_SUCCESS;
import static com.c88.game.adapter.constants.AELiveConstants.TURN_IN_URL;
import static com.c88.game.adapter.constants.AELiveConstants.TURN_OUT_URL;
import static com.c88.game.adapter.constants.AELiveConstants.WITHDRAW_DEFAULT;

@Slf4j
@Component
@RequiredArgsConstructor
public class AELiveGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final IPlatformService iplatformService;

    private final RestTemplate restTemplate;

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
        return PLATFORM_CODE;
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {
        String url = apiUrl + LOGIN_URL;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cert", apiKey);
        map.add("agentId", apiId);
        map.add("userId", this.getUsername(username));
        map.add("platform", PLATFORM_LIVE_SEXYBCRT);
        map.add("gameType", GAME_TYPE);
        map.add("gameCode", GAME_CODE);

        HttpEntity<MultiValueMap<String, String>> entity = generateHttpEntity(map);

        log.info("AE login url: {}, jsonObject : {}", url, JSON.toJSONString(entity));

        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        String status = jsonResult.getString("status");

        if (!AELiveConstants.RESPONSE_SUCCESS.equals(status)) {
            return Result.failed(jsonResult.getString("desc"));
        }
        return Result.success(jsonResult.getString("url"));
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        JSONObject jsonLimitId = new JSONObject();
        JSONObject jsonLive = new JSONObject();
        JSONObject jsonLivePlatform = new JSONObject();
        jsonLimitId.put("limitId", LIVE_BET_LIMIT_ID);
        jsonLive.put("SEXYBCRT", jsonLivePlatform);
        jsonLivePlatform.put("LIVE", jsonLimitId);

        String url = apiUrl + REGISTER_URL;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cert", apiKey);
        map.add("agentId", apiId);
        map.add("userId", this.getUsername(username));
        map.add("currency", CURRENCY);
        map.add("betLimit", jsonLive.toJSONString());
        map.add("language", LANG_VI);

        HttpEntity<MultiValueMap<String, String>> entity = generateHttpEntity(map);

        log.info("AE register url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        log.info("AE register url result:{}", jsonResult);

        String status = jsonResult.getString("status");
        if (!List.of(RESPONSE_SUCCESS).contains(status)) {
            return Result.failed(jsonResult.getString("desc"));
        }

        if (!List.of(ACCOUNT_EXIST).contains(status)) {
            return Result.success(username);
        }

        iPlatformGameMemberService.save(
                PlatformGameMember.builder()
                        .memberId(memberId)
                        .username(this.getUsername(username))
                        .platformId(platform.getId())
                        .code(PLATFORM_CODE)
                        .build()
        );
        return Result.success(this.getUsername(username));
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        if (Objects.equals(this.enable, EnableEnum.STOP.getCode())) {
            return Result.success(BigDecimal.ZERO);
        }

        String url = apiUrl + BALANCE_URL;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cert", apiKey);
        map.add("agentId", apiId);
        map.add("userIds", this.getUsername(username));
        map.add("alluser", BALANCE_BY_USER);

        HttpEntity<MultiValueMap<String, String>> entity = generateHttpEntity(map);

        log.info("AE findMemberBalance url: {}", entity);
        String jsonResultStr;
        try {
            jsonResultStr = restTemplate.postForObject(url, entity, String.class);
        } catch (Exception e) {
            log.info("AE Live balance Exception : {}", ExceptionUtil.getRootCauseMessage(e));
            return Result.success(BigDecimal.ZERO);
        }
        log.info("AE findMemberBalance url result:{}", jsonResultStr);
        JSONObject jsonResult = JSON.parseObject(jsonResultStr);

        String status = jsonResult.getString("status");
        if (!Objects.equals(RESPONSE_SUCCESS, status)) {
            return Result.failed(jsonResult.getString("desc"));
        }

        return Result.success(BigDecimal.ZERO);
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        String url = apiUrl + TURN_IN_URL;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cert", apiKey);
        map.add("agentId", apiId);
        map.add("userId", this.getUsername(username));
        map.add("transferAmount", String.valueOf(amount));
        map.add("txCode", transactionNo);

        log.info("AE transfer in url: {}, jsonObject : {}", url, JSON.toJSONString(map));
        HttpEntity<MultiValueMap<String, String>> entity = generateHttpEntity(map);
        String jsonResultStr = restTemplate.postForObject(url, entity, String.class);
        log.info("AE transfer in url result:{}", jsonResultStr);
        JSONObject jsonResult = JSON.parseObject(jsonResultStr);
        String status = jsonResult.getString("status");

        if (AELiveConstants.RESPONSE_SUCCESS.equals(status)) {
            TransferStateVO stateVO = new TransferStateVO();
            stateVO.setBalance(jsonResult.getBigDecimal("amount"));
            stateVO.setState(AdapterTransferStateEnum.SUCCESS);
            return Result.success(stateVO);
        }
        return Result.failed(jsonResult.getString("desc"));

    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        String url = apiUrl + TURN_OUT_URL;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cert", apiKey);
        map.add("agentId", apiId);
        map.add("userId", this.getUsername(username));
        map.add("transferAmount", String.valueOf(amount));
        map.add("txCode", transactionNo);
        map.add("withdrawType", WITHDRAW_DEFAULT);

        HttpEntity<MultiValueMap<String, String>> entity = generateHttpEntity(map);
        log.info("AE transfer out url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
        String jsonResultStr = restTemplate.postForObject(url, entity, String.class);
        log.info("AE transfer out url result:{}", jsonResultStr);
        JSONObject jsonResult = JSON.parseObject(jsonResultStr);
        String status = jsonResult.getString("status");

        if (AELiveConstants.RESPONSE_SUCCESS.equals(status)) {
            TransferStateVO stateVO = new TransferStateVO();
            stateVO.setBalance(jsonResult.getBigDecimal("amount"));
            stateVO.setState(AdapterTransferStateEnum.SUCCESS);
            return Result.success(stateVO);
        }
        return Result.failed(jsonResult.getString("desc"));
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        String url = apiUrl + AELiveConstants.TURN_STATUS_URL;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cert", apiKey);
        map.add("agentId", apiId);
        map.add("txCode", orderId);

        HttpEntity<MultiValueMap<String, String>> entity = generateHttpEntity(map);
        log.info("AE findTicketStatus url: {}, jsonObject : {}", url, JSON.toJSONString(map));

        String response = restTemplate.postForObject(url, entity, String.class);
        log.info("AE findTicketStatus out url result:{}", response);
        JSONObject jsonResult = JSON.parseObject(response);
        String status = jsonResult.getString("status");

        if (AELiveConstants.RESPONSE_SUCCESS.equals(status)) {
            return Result.success(response);
        }
        return Result.failed(jsonResult.getString("desc"));
    }

    @Override
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        List<AELiveTransactionVO> transactions = this.fetchBetOrder(startDateTime, endDateTime);
        this.saveOrder(transactions);
    }

    @Override
    public void doFetchBetOrderAction() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = LocalDateTime.now();
        List<AELiveTransactionVO> transactions = this.fetchBetOrder(startTime, endTime);
        this.saveOrder(transactions);
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    @Override
    public List<AELiveTransactionVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+00:00");
        String url = apiUrl + BET_RECORD_URL_BY_TIME;

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cert", apiKey);
        map.add("agentId", apiId);
        map.add("startTime", startTime.format(dtf));
        map.add("endTime", endTime.format(dtf));
        map.add("platform", "SEXYBCRT");

        HttpEntity<MultiValueMap<String, String>> entity = generateHttpEntity(map);

        log.info("AE login url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
        AELiveBetOrderListVO aeLiveBetOrderResult = restTemplate.postForObject(url, entity, AELiveBetOrderListVO.class);
        if (!AELiveConstants.RESPONSE_SUCCESS.equals(aeLiveBetOrderResult.getStatus())) {
            throw new RuntimeException("AE fetchBetOrder fail " + aeLiveBetOrderResult.getStatus());
        }

        return aeLiveBetOrderResult.getTransactions();
    }

    @Transactional
    public void saveOrder(List<AELiveTransactionVO> datasource) {
        datasource.parallelStream()
                .forEach(ae -> {
                            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(
                                    PLATFORM_CODE, ae.getUserId());
                            if (platformGameMember == null) {
                                return;
                            }

                            BetOrderVO betOrder = BetOrderVO.builder()
                                    .transactionNo(ae.getPlatform() + "_" + ae.getPlatformTxId())
                                    .transactionSerial(ae.getPlatform() + "_" + ae.getPlatformTxId())
                                    .transactionTime(LocalDateTime.ofInstant(Instant.parse(ae.getBetTime()), ZoneOffset.UTC))
                                    .memberId(platformGameMember.getMemberId())
                                    .username(platformGameMember.getUsername().replaceAll(prefix, ""))
                                    .platformId(platformGameMember.getPlatformId())
                                    .gameId(ae.getGameCode())
                                    .platformCode(ae.getPlatform())
                                    .gameName(ae.getGameName())
                                    .gameCategoryCode(GAME_TYPE)
                                    .validBetAmount(ae.getTurnover())
                                    .betAmount(ae.getBetAmount())
                                    .settle(ae.getRealWinAmount())
                                    .settleTime(LocalDateTime.ofInstant(Instant.parse(ae.getUpdateTime()), ZoneOffset.UTC))
                                    .winLoss(ae.getWinAmount())
                                    .betState(ae.getTxStatus() == 1 ? BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue() : BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())
                                    .rebateState(ae.getTxStatus() == 1 ? ReBateStatusEnum.UN_SETTLED.getValue() : ReBateStatusEnum.SETTLED.getValue())
                                    .build();

                            //發送Mq
                            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);
                        }
                );

    }

    private HttpEntity<MultiValueMap<String, String>> generateHttpEntity(MultiValueMap<String, String> map) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(map, httpHeaders);
    }

}
