package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.adapter.form.FindSABONGBetFilterForm;
import com.c88.game.adapter.service.third.adapter.form.FindSABONGBetForm;
import com.c88.game.adapter.service.third.vo.SABONGBetOrderListVO;
import com.c88.game.adapter.service.third.vo.SABONGBetOrderVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.c88.game.adapter.constants.SABONGConstants.BALANCE_URL;
import static com.c88.game.adapter.constants.SABONGConstants.GAME_CATEGORY_CODE;
import static com.c88.game.adapter.constants.SABONGConstants.LOGIN_URL;
import static com.c88.game.adapter.constants.SABONGConstants.PLATFORM_CODE;
import static com.c88.game.adapter.constants.SABONGConstants.PLAYER_TOKEN_URL;
import static com.c88.game.adapter.constants.SABONGConstants.REGISTER_URL;
import static com.c88.game.adapter.constants.SABONGConstants.TOKEN_URL;
import static com.c88.game.adapter.constants.SABONGConstants.TURN_IN_URL;
import static com.c88.game.adapter.constants.SABONGConstants.TURN_OUT_URL;
import static com.c88.game.adapter.constants.SABONGConstants.TURN_STATUS_URL;
import static com.c88.game.adapter.constants.SABONGConstants.USERNAME_EXISTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class SABONGGameAdapter implements IGameAdapter {

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private String prefix;
    private String apiId;
    private String apiKey;
    private String apiUrl;
    private String recordUrl;
    private String locale;

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
        String token = getPlayerToken(username);

        String url = apiUrl + LOGIN_URL;
        Map<String, String> params = Map.of("token", token);
        HttpEntity<JSONObject> request = generateHttpEntity(null, generateAuthorization(token));

        log.info("SABONG before login url: {}, jsonObject : {}", url, request);
        ResponseEntity<JSONObject> resultJsonEntity = restTemplate.exchange(url + "?player-token={token}", HttpMethod.GET, request, JSONObject.class, params);
        log.info("SABONG login jsonResult:{}", resultJsonEntity);

        JSONObject body = resultJsonEntity.getBody();
        if (resultJsonEntity.getStatusCodeValue() == 200) {
            return Result.success(body.getString("url"));
        }

        return Result.success("");
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        String token = getClientToken(username);

        String url = apiUrl + REGISTER_URL;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("playerNumber", this.getUsername(username));
        jsonObject.put("playerName", this.getUsername(username));
        jsonObject.put("playerAlias", username);
        jsonObject.put("language", "EN");
        jsonObject.put("currency", "PHP");
        jsonObject.put("minBetLimit", new BigDecimal("1"));
        jsonObject.put("maxBetLimit", new BigDecimal("10000000"));
        jsonObject.put("brandCode", "C88BET");

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject, generateAuthorization(token));

        log.info("SABONG register url: {}, entity : {}", url, entity);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(url, entity, JSONObject.class);
        log.info("SABONG register response : {}", jsonObjectResponseEntity);

        JSONObject body = jsonObjectResponseEntity.getBody();
        if (jsonObjectResponseEntity.getStatusCodeValue() != 200) {
            log.info("SABONG register fail : {}", jsonObjectResponseEntity);
            throw new RuntimeException("SABONG register fail");
        }

        JSONObject jsonStatus = body.getJSONObject("status");

        String status = jsonStatus.toJSONString();
        if (!USERNAME_EXISTS.equals(status)) {
            return Result.failed(status);
        }

        // 寫入新帳號
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
        String token = getClientToken(username);

        String url = apiUrl + BALANCE_URL;
        Map<String, String> params = Map.of("playerNumber", this.getUsername(username));
        HttpEntity<JSONObject> request = generateHttpEntity(null, generateAuthorization(token));

        log.info("SABONG balance url: {}, jsonObject : {}", url, request);

        ResponseEntity<JSONObject> resultJsonEntity;
        try {
            resultJsonEntity = restTemplate.exchange(url + "?playerNumber={playerNumber}", HttpMethod.GET, request, JSONObject.class, params);
        } catch (Exception e) {
            log.info("SABONG balance Exception : {}", ExceptionUtil.getRootCauseMessage(e));
            return Result.success(BigDecimal.ZERO);
        }
        log.info("SABONG balance jsonObject : {}", resultJsonEntity);

        JSONObject body = resultJsonEntity.getBody();
        if (resultJsonEntity.getStatusCodeValue() == 200) {
            log.info("SABONG balance api fail : {}", resultJsonEntity);
            return Result.success(body.getBigDecimal("balance"));
        }

        return Result.success(BigDecimal.ZERO);
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        String token = getClientToken(username);

        String url = apiUrl + TURN_IN_URL;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("playerNumber", this.getUsername(username));
        jsonObject.put("amount", amount);
        jsonObject.put("clientTransactionId", transactionNo);

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject, generateAuthorization(token));

        log.info("SABONG transfer in url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
        try {
            ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(url, entity, JSONObject.class);
            log.info("SABONG transfer in result : {}", jsonObjectResponseEntity);

            JSONObject jsonResult = jsonObjectResponseEntity.getBody();
            if (jsonObjectResponseEntity.getStatusCodeValue() == 200) {
                return Result.success(
                        TransferStateVO.builder()
                                .balance(jsonResult.getBigDecimal("balance"))
                                .state(AdapterTransferStateEnum.SUCCESS)
                                .build()
                );
            }

            return Result.success(
                    TransferStateVO.builder()
                            .balance(BigDecimal.ZERO)
                            .state(AdapterTransferStateEnum.FAIL)
                            .build()
            );
        }catch (Exception ex){
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        String token = getClientToken(username);

        String url = apiUrl + TURN_OUT_URL;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("playerNumber", this.getUsername(username));
        jsonObject.put("amount", amount);
        jsonObject.put("clientTransactionId", transactionNo);

        HttpEntity<JSONObject> entity = generateHttpEntity(jsonObject, generateAuthorization(token));

        try {
            log.info("SABONG transfer out url: {}, jsonObject : {}", url, JSON.toJSONString(entity));
            ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(url, entity, JSONObject.class);
            log.info("SABONG transfer out result : {}", jsonObjectResponseEntity);

            JSONObject jsonResult = jsonObjectResponseEntity.getBody();
            if (jsonObjectResponseEntity.getStatusCodeValue() == 200) {
                return Result.success(
                        TransferStateVO.builder()
                                .balance(jsonResult.getBigDecimal("balance"))
                                .state(AdapterTransferStateEnum.SUCCESS)
                                .build()
                );
            }

            return Result.success(
                    TransferStateVO.builder()
                            .balance(BigDecimal.ZERO)
                            .state(AdapterTransferStateEnum.FAIL)
                            .build()
            );
        }catch (Exception ex){
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        String token = getClientToken(username);

        String url = apiUrl + TURN_STATUS_URL;

        FindSABONGBetForm form = FindSABONGBetForm.builder()
                .pageNumber(1)
                .pageSize(1)
                .filter(List.of(FindSABONGBetFilterForm.builder().fieldName("operatorCode").fieldValue1(orderId).type("string").operand("eq").build()))
                .build();

        HttpEntity<JSONObject> entity = generateHttpEntity((JSONObject) JSON.toJSON(form), generateAuthorization(token));
        log.info("SABONG findTicketStatus url: {}, jsonObject : {}", url, entity);
        ResponseEntity<SABONGBetOrderVO> jsonObjectResponseEntity = restTemplate.postForEntity(url, entity, SABONGBetOrderVO.class);
        log.info("SABONG findTicketStatus result", jsonObjectResponseEntity);

        SABONGBetOrderVO body = jsonObjectResponseEntity.getBody();
        if (jsonObjectResponseEntity.getStatusCodeValue() == 200) {
        }

        return Result.success(null);
    }

    @Override
    public void manualBetOrder(LocalDateTime startTime, LocalDateTime endTime, Map<String, String> param) {
        Result<SABONGBetOrderVO> aeLotteryBertOrderVOResult = this.fetchBetOrder(startTime, endTime, 0, 500);
        if (Result.isSuccess(aeLotteryBertOrderVOResult)) {
            this.saveOrder(aeLotteryBertOrderVOResult.getData().getList());
        }
    }

    @Override
    public void doFetchBetOrderAction() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = LocalDateTime.now();

        int page = 0;
        int pageSize = 500;
        Result<SABONGBetOrderVO> aeLotteryBertOrderVOResult = this.fetchBetOrder(startTime, endTime, page, pageSize);

        if (Result.isFail(aeLotteryBertOrderVOResult)) {
            return;
        }

        SABONGBetOrderVO data = aeLotteryBertOrderVOResult.getData();
        List<SABONGBetOrderListVO> datasource = data.getList();
        for (int x = pageSize; x < data.getRecordCount(); x = x + pageSize) {
            Result<SABONGBetOrderVO> result = this.fetchBetOrder(startTime, endTime, page, pageSize);
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
    public List<SABONGBetOrderListVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        int page = 0;
        int pageSize = 500;

        Result<SABONGBetOrderVO> aeLotteryBertOrderVOResult = this.fetchBetOrder(startTime, endTime, page, pageSize);

        if (Result.isFail(aeLotteryBertOrderVOResult)) {
            return Collections.emptyList();
        }
        SABONGBetOrderVO data = aeLotteryBertOrderVOResult.getData();
        List<SABONGBetOrderListVO> datasource = data.getList();
        for (int x = pageSize; x < data.getRecordCount(); x = x + pageSize) {
            Result<SABONGBetOrderVO> result = this.fetchBetOrder(startTime, endTime, page, pageSize);
            if (Result.isFail(result)) {
                break;
            }
            datasource.addAll(result.getData().getList());
        }

        return datasource;
    }

    private Result<SABONGBetOrderVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        // 取得token需要有會員帳號
        String token = getClientToken("admin");

        String url = apiUrl + TURN_STATUS_URL;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        FindSABONGBetForm form = FindSABONGBetForm.builder()
                .pageNumber(page)
                .pageSize(pageSize)
                .filter(List.of(
                                FindSABONGBetFilterForm.builder()
                                        .fieldName("createdDate")
                                        .fieldValue1(startTime.format(dtf))
                                        .fieldValue2(endTime.format(dtf))
                                        .type("datetime")
                                        .operand("between").build()
                        )
                )
                .build();

        HttpEntity<JSONObject> entity = generateHttpEntity((JSONObject) JSON.toJSON(form), generateAuthorization(token));
        log.info("SABONG findTicketStatus url: {}, jsonObject : {}", url, entity);
        ResponseEntity<SABONGBetOrderVO> jsonObjectResponseEntity = restTemplate.postForEntity(url, entity, SABONGBetOrderVO.class);
        log.info("SABONG findTicketStatus result", jsonObjectResponseEntity);

        SABONGBetOrderVO body = jsonObjectResponseEntity.getBody();
        if (jsonObjectResponseEntity.getStatusCodeValue() == 200) {
            return Result.success(body);
        }

        log.info("SABONG fetchBetOrder fail : {}", body);
        throw new RuntimeException("SABONG fetchBetOrder fail");
    }

    @Transactional
    public boolean saveOrder(List<SABONGBetOrderListVO> datasource) {
        datasource.parallelStream().forEach(ae ->
                {
                    PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(
                            PLATFORM_CODE, ae.getPlayerNumber());
                    if (Objects.isNull(platformGameMember)) {
                        return;
                    }

                    BetOrderVO betOrder = BetOrderVO.builder()
                            .transactionNo(ae.getTransactionId())
                            .transactionSerial(ae.getOperatorCode())
                            // .transactionTime(LocalDateTime.ofEpochSecond(ae.getBetAt(), 0, ZoneOffset.UTC))
                            .memberId(platformGameMember.getMemberId())
                            .username(platformGameMember.getUsername().replaceAll(prefix, ""))
                            .platformId(platformGameMember.getPlatformId())
                            .gameId(ae.getBetCorner())
                            .platformCode(PLATFORM_CODE)
                            .gameName(ae.getBetCorner())
                            .gameCategoryCode(GAME_CATEGORY_CODE)
                            .validBetAmount(ae.getBetAmount())
                            .betAmount(ae.getBetAmount())
                            .settle(ae.getWinAmount())
                            // .settleTime(LocalDateTime.ofEpochSecond(ae.getPaidAt(), 0, ZoneOffset.UTC))
                            .winLoss(ae.getWinAmount())
                            // .betState(ae.getStatus() == 1 ? BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue() : BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())
                            // .rebateState(ae.getStatus() == 1 ? ReBateStatusEnum.UN_SETTLED.getValue() : ReBateStatusEnum.SETTLED.getValue())
                            .build();

                    //發送Mq
                    // kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);
                }
        );

        return Boolean.TRUE;
    }

    /**
     * 產生商戶HEADER
     *
     * @param jsonObject
     * @param token
     * @return
     */
    private HttpEntity<JSONObject> generateHttpEntity(JSONObject jsonObject, String token) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("x-client-key", apiKey);
        httpHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.isNotBlank(token)) {
            httpHeaders.add("Authorization", token);
        }

        return new HttpEntity<>(jsonObject, httpHeaders);
    }

    /**
     * 產生令牌
     *
     * @param token
     * @return
     */
    private String generateAuthorization(String token) {
        return String.format("Bearer %s", token);
    }

    /**
     * 取得用戶端token
     *
     * @param username
     * @return
     */
    private String getClientToken(String username) {
        String playerTokenUrl = apiUrl + TOKEN_URL + "?playerNumber=" + this.getUsername(username);
        HttpEntity<JSONObject> entity = generateHttpEntity(null, null);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(URI.create(playerTokenUrl), entity, JSONObject.class);

        if (jsonObjectResponseEntity.getStatusCodeValue() == 200) {
            log.info("SABONG token : {}, ", jsonObjectResponseEntity);
            return jsonObjectResponseEntity.getBody().getString("token");
        }

        log.info("SABONG getClientToken fail : {}", jsonObjectResponseEntity);
        throw new RuntimeException("SABONG getClientToken fail");
    }

    /**
     * 取得用戶端token
     *
     * @param username
     * @return
     */
    private String getPlayerToken(String username) {
        String playerTokenUrl = apiUrl + PLAYER_TOKEN_URL + "?playerNumber=" + this.getUsername(username);
        HttpEntity<JSONObject> entity = generateHttpEntity(null, null);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(URI.create(playerTokenUrl), entity, JSONObject.class);

        if (jsonObjectResponseEntity.getStatusCodeValue() == 200) {
            log.info("SABONG token : {}, ", jsonObjectResponseEntity);
            return jsonObjectResponseEntity.getBody().getString("token");
        }

        log.info("SABONG getClientToken fail : {}", jsonObjectResponseEntity);
        throw new RuntimeException("SABONG getClientToken fail");
    }

}
