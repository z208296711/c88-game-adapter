package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.HttpUtil;
import com.c88.game.adapter.dto.PGVerifySessionDTO;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.FetchRecord;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.service.IFetchRecordService;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.IRetryFetchOrder;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.PGBetOrderVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.utils.EncryptUtil;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.game.adapter.vo.PGVerifySessionDataVO;
import com.c88.game.adapter.vo.PGVerifySessionErrorVO;
import com.c88.game.adapter.vo.PGVerifySessionVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.c88.game.adapter.constants.PGConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PGGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private final IFetchRecordService iFetchRecordService;
    private final IRetryFetchOrder retryFetchOrderImpl;

    private String operatorToken;

    private String secretKey;

    private String apiUrl;

    private String gameUrl;

    private String recordUrl;

    private String prefix;

    private String suffix;

    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.operatorToken = apiParameter.getApiId();
        this.secretKey = apiParameter.getApiKey();
        this.apiUrl = apiParameter.getApiUrl();
        this.gameUrl = apiParameter.getGameUrl();
        this.recordUrl = apiParameter.getRecordUrl();
        this.prefix = apiParameter.getPrefix();
        this.suffix = apiParameter.getSuffix();
    }

    @Override
    public String getUsername(String username) {
        return prefix + username + suffix;
    }

    private String getOriginUsername(String username) {
        return username.substring(prefix.length(), username.length() - suffix.length());
    }

    @Override
    public String getGamePlatformCode() {
        return PLATFORM_CODE;
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {
        final String url = apiUrl + LOGIN_URL + "?trace_id=" + UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

//        驗證令牌
        String playerSession = EncryptUtil.MD5(username).toUpperCase();

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("operator_token", operatorToken);
        map.add("secret_key", secretKey);
        map.add("player_session", playerSession);
        map.add("player_name", this.getUsername(username));
        map.add("currency", CURRENCY_DEFAULT);
        log.info("JIL login url: {}, queryMap : {}", url, map);
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (HttpStatus.OK.equals(httpStatus) && jsonBody.get("error") == null) {
            redisTemplate.opsForValue().set(playerSession, this.getUsername(username));
            String gameId = param.get("GameId");

            Map<String, Object> map2 = new LinkedHashMap<>();
            map2.put("btt", 1);
            map2.put("ot", operatorToken);
            try {
                map2.put("ops", URLEncoder.encode(playerSession, "UTF-8"));
            } catch (UnsupportedEncodingException e) {

            }
            String queryString = HttpUtil.parseMapToString(map2);
            String loginUrl = gameUrl + gameId + "/index.html?" + queryString;
            return Result.success(loginUrl);
        }
        return Result.failed(jsonBody.getJSONObject("data").getString("errors"));
    }

    public PGVerifySessionVO verifyLoginSession(PGVerifySessionDTO inputDTO) {
        PGVerifySessionDataVO pgVerifySessionDataVO;
        PGVerifySessionErrorVO pgVerifySessionErrorVO;
        PGVerifySessionVO pgVerifySessionVO = new PGVerifySessionVO();
        if (secretKey.equals(inputDTO.getSecret_key()) && operatorToken.equals(inputDTO.getOperator_token())) {
            try {
                String playerSession = URLDecoder.decode(inputDTO.getOperator_player_session(), "UTF-8");
                String username = (String) redisTemplate.opsForValue().get(playerSession);
                if (username == null) {
                    log.info("PG login 驗證session不存在 inputDTO : {}", inputDTO);
                } else {
                    pgVerifySessionDataVO = new PGVerifySessionDataVO();
                    pgVerifySessionDataVO.setPlayer_name(username);
                    pgVerifySessionDataVO.setCurrency(CURRENCY_DEFAULT);
                    pgVerifySessionVO.setData(pgVerifySessionDataVO);
                    return pgVerifySessionVO;
                }
            } catch (UnsupportedEncodingException e) {
                log.info("PG login 驗證解碼失敗 inputDTO : {}", inputDTO);
            }
        } else {
            log.info("PG login 密鑰錯誤 inputDTO : {}", inputDTO);
        }
        pgVerifySessionErrorVO = new PGVerifySessionErrorVO();
        pgVerifySessionErrorVO.setCode("1034");
        pgVerifySessionErrorVO.setMessage("无效请求");
        pgVerifySessionVO.setError(pgVerifySessionErrorVO);
        return pgVerifySessionVO;
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        Result<String> result = this.login(username, Map.of("GameId", "7"));
        if (!Result.isSuccess(result)) {
            return result;
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

    @Override
    public Result<BigDecimal> balance(String username) {
        final String url = apiUrl + BALANCE_URL + "?trace_id=" + UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("operator_token", operatorToken);
        map.add("secret_key", secretKey);
        map.add("player_name", this.getUsername(username));
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (HttpStatus.OK.equals(httpStatus) && jsonBody.getString("error") == null) {
            BigDecimal balance = jsonBody.getJSONObject("data").getBigDecimal("cashBalance");
            return Result.success(balance);
        }
        return Result.failed(jsonBody.getJSONObject("error").getString("message"));
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        final String url = apiUrl + TURN_IN_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("operator_token", operatorToken);
        map.add("secret_key", secretKey);
        map.add("player_name", this.getUsername(username));
        map.add("amount", amount);
        map.add("transfer_reference", transactionNo);
        map.add("currency", CURRENCY_DEFAULT);
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (!HttpStatus.OK.equals(httpStatus) || jsonBody.get("error") != null) {
            return Result.failed(jsonBody.getString("error"));
        }
        return Result.success(TransferStateVO.builder().
                balance(amount).
                state(AdapterTransferStateEnum.SUCCESS).
                build());
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        final String url = apiUrl + TURN_OUT_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("operator_token", operatorToken);
        map.add("secret_key", secretKey);
        map.add("player_name", this.getUsername(username));
        map.add("amount", amount);
        map.add("transfer_reference", transactionNo);
        map.add("currency", CURRENCY_DEFAULT);
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (!HttpStatus.OK.equals(httpStatus) || jsonBody.get("error") != null) {
            return Result.failed(jsonBody.getJSONObject("error").getString("message"));
        }
        return Result.success(TransferStateVO.builder().
                balance(amount).
                state(AdapterTransferStateEnum.SUCCESS).
                build());
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        final String url = apiUrl + TURN_STATUS_URL + "?trace_id=" + UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("operator_token", operatorToken);
        map.add("secret_key", secretKey);
        map.add("player_name", this.getUsername(username));
        map.add("transfer_reference", orderId);
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (HttpStatus.OK.equals(httpStatus) && jsonBody.getString("error") == null) {
            return Result.success("TRANSFER_SUCCESS");
        }
        return Result.success("TRANSFER_FAIL");
    }

    /**
     * 手動補單
     *
     * @param startDateTime
     * @param endDateTime
     * @param param
     * @throws Exception
     */
    @Override
    @Transactional
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        List<PGBetOrderVO> dataList = this.fetchBetOrder(startDateTime, endDateTime);
        this.saveOrder(dataList);
    }

    /**
     * 拉取注單
     */
    @Override
    @Transactional
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
            List<PGBetOrderVO> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("PG_exception:{}",ex.toString());
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
                if(CollectionUtils.isNotEmpty((List<PGBetOrderVO>)retryFetchOrder)) {
                    this.saveOrder((List<PGBetOrderVO>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    public List<PGBetOrderVO> fetchBetOrder(LocalDateTime start, LocalDateTime end) {
        final String url = recordUrl + BET_RECORD_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("operator_token", operatorToken);
        map.add("secret_key", secretKey);
        map.add("count", 5000);
        map.add("bet_type", 1);
        map.add("from_time", ZonedDateTime.of(start, ZoneId.systemDefault()).toInstant().toEpochMilli());
        map.add("to_time", ZonedDateTime.of(end, ZoneId.systemDefault()).toInstant().toEpochMilli());
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (HttpStatus.OK.equals(httpStatus) && jsonBody.getString("error") == null) {
            return jsonBody.getJSONArray("data").toJavaList(PGBetOrderVO.class);
        }
        return new ArrayList<>();
    }

    @Transactional
    public Long saveOrder(List<PGBetOrderVO> list) {
        AtomicReference<Long> version = new AtomicReference<>(0L);
        list.forEach(vo -> {
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, vo.getPlayerName());
            if (platformGameMember == null) {
                return;
            }
            Integer status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getBetId());
            betOrder.setTransactionSerial(vo.getBetId());
            betOrder.setTransactionTime(toLocalDateTime(vo.getBetTime()));

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

            betOrder.setGameId(vo.getGameId());
            betOrder.setGameName("PG : " + vo.getGameId());
            betOrder.setGameCategoryCode("slot");
            betOrder.setValidBetAmount(vo.getBetAmount());
            betOrder.setBetAmount(vo.getBetAmount());
            betOrder.setSettle(vo.getWinAmount());
            betOrder.setSettleTime(toLocalDateTime(vo.getBetEndTime()));

            betOrder.setWinLoss(vo.getWinAmount().subtract(vo.getBetAmount()));
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(PLATFORM_CODE);

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

        });
        return version.get();
    }

    private static LocalDateTime toLocalDateTime(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());

    }
}
