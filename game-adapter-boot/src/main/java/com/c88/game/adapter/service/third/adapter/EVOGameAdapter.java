package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.DateUtil;
import com.c88.common.core.util.HttpUtil;
import com.c88.common.web.util.HttpUtils;
import com.c88.game.adapter.dto.EvoPlayerDTO;
import com.c88.game.adapter.dto.EvoSessionDTO;
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
import com.c88.game.adapter.service.third.vo.EvoBetOrderVO;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.utils.EncryptUtil;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.c88.game.adapter.constants.EvoConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EVOGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private final IFetchRecordService iFetchRecordService;
    private final IRetryFetchOrder retryFetchOrderImpl;

    private String token;

    private String casinoKey;

    private String apiUrl;

    private String adminUrl;

    private String prefix;

    private String suffix;

    public static final String formatString = "YYYY-MM-dd'T'HH:mm:ss.SSS'Z'";

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.token = apiParameter.getApiId();
        this.casinoKey = apiParameter.getApiKey();
        this.apiUrl = apiParameter.getApiUrl();
        this.adminUrl = apiParameter.getRecordUrl();
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
        final String url = apiUrl + LOGIN_URL + "/" + casinoKey + "/" + token;
        EvoPlayerDTO playerDTO = new EvoPlayerDTO();
        username = this.getUsername(username);
        playerDTO.setId(username);
        playerDTO.setFirstName(username);
        playerDTO.setLastName(username);
        playerDTO.setCountry(COUNTRY_DEFAULT);
        playerDTO.setCurrency(CURRENCY_DEFAULT);
        playerDTO.setLanguage(LANG_DEFAULT);
        playerDTO.setUpdate(true);
        playerDTO.getSession().setId(EncryptUtil.MD5(username));
        playerDTO.getSession().setIp(param.get("ip"));
        JSONObject gameRequest = new JSONObject();
        EvoSessionDTO tableDto = new EvoSessionDTO();
        tableDto.setId(param.get("GameId"));
        gameRequest.put("table", tableDto);
        JSONObject requestBody = new JSONObject();
        requestBody.put("player", playerDTO);
        requestBody.put("config", gameRequest);
        log.info("EVO login url: {}, playerDTO : {}", url, playerDTO);
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, requestBody, JSONObject.class);
        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();
        if (HttpStatus.OK.equals(httpStatus)) {
            return Result.success(jsonBody.getString("entry"));
        }
        HashMap<String, String> errorMsg = (HashMap) jsonBody.getJSONArray("errors").get(0);
        return Result.failed(errorMsg.get("message"));
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        Result<String> result = this.login(username, Map.of("ip", HttpUtils.getClientIp()));
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
        final String url = apiUrl + ECASH_URL;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cCode", BALANCE);
        map.put("ecID", casinoKey);
        map.put("euID", this.getUsername(username));
        map.put("output", "0");
        String queryString = HttpUtil.parseMapToString(map);
        log.info("EVO bet log url: {}, queryString : {}", url, queryString);

        try {
            ResponseEntity<String> result = restTemplate.getForEntity(URI.create(url + "?" + queryString), String.class);

            HttpStatus httpStatus = result.getStatusCode();
            JSONObject jsonBody = JSONObject.parseObject(result.getBody());

            if (HttpStatus.OK.equals(httpStatus)) {
                if (jsonBody.keySet().contains("error"))
                    return Result.failed(jsonBody.getJSONObject("error").getString("errormsg"));
                return Result.success(jsonBody.getJSONObject("userbalance").getBigDecimal("tbalance"));
            }
            return Result.failed(jsonBody.getJSONObject("error").getString("errormsg"));
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, TURN_IN);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, TURN_OUT);
    }

    private Result<TransferStateVO> transfer(String username, BigDecimal amount, String transactionNo, String cCode) {
        final String url = apiUrl + ECASH_URL;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cCode", cCode);
        map.put("ecID", casinoKey);
        map.put("euID", this.getUsername(username));
        map.put("amount", amount);
        map.put("eTransID", transactionNo);
        map.put("output", "0");
        String queryString = HttpUtil.parseMapToString(map);

        try {
            ResponseEntity<String> result = restTemplate.getForEntity(URI.create(url + "?" + queryString), String.class);

            HttpStatus httpStatus = result.getStatusCode();
            JSONObject jsonBody = JSONObject.parseObject(result.getBody());

            if (HttpStatus.OK.equals(httpStatus)) {
                JSONObject transfer = jsonBody.getJSONObject("transfer");
                if ("Y".equals(transfer.getString("result"))) {
                    return Result.success(
                            TransferStateVO.builder()
                                    .balance(amount)
                                    .state(AdapterTransferStateEnum.SUCCESS)
                                    .build()
                    );
                }
                log.error("EVO transfer faile:{}", transfer.getString("errormsg"));
            }
        } catch (Exception e) {
            log.error("EVO transfer Exception : {}", ExceptionUtil.stacktraceToString(e));
            return Result.success(
                    TransferStateVO.builder()
                            .balance(amount)
                            .state(AdapterTransferStateEnum.UNKNOWN)
                            .build()
            );
        }

        return Result.success(
                TransferStateVO.builder()
                        .balance(amount)
                        .state(AdapterTransferStateEnum.FAIL)
                        .build()
        );
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        final String url = apiUrl + ECASH_URL;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cCode", TRAN_INFO);
        map.put("ecID", casinoKey);
        map.put("euID", this.getUsername(username));
        map.put("eTransID", orderId);
        map.put("output", "0");
        String queryString = HttpUtil.parseMapToString(map);
        ResponseEntity<String> result = restTemplate.getForEntity(URI.create(url + "?" + queryString), String.class);

        HttpStatus httpStatus = result.getStatusCode();
        JSONObject jsonBody = JSONObject.parseObject(result.getBody());

        if (HttpStatus.OK.equals(httpStatus)) {
            if (jsonBody.keySet().contains("error"))
                return Result.success("TRANSFER_FAIL");
            return Result.success("TRANSFER_SUCCESS");
        }
        return Result.failed(jsonBody.getJSONObject("error").getString("errormsg"));
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
        List<EvoBetOrderVO> dataList = this.fetchBetOrder(startDateTime, endDateTime);
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
            List<EvoBetOrderVO> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("EVO_exception:{}",ex.toString());
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
                if(CollectionUtils.isNotEmpty((List<EvoBetOrderVO>)retryFetchOrder)) {
                    this.saveOrder((List<EvoBetOrderVO>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    public List<EvoBetOrderVO> fetchBetOrder(LocalDateTime start, LocalDateTime end) {
        final String url = adminUrl + BET_RECORD_URL;
        String notEncoded = casinoKey + ":" + token;
        String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(notEncoded.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", encodedAuth);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("startDate", DateUtil.dateToStr(start, formatString));
        String queryString = HttpUtil.parseMapToString(map);
        log.info("EVO bet log url: {}, queryString : {}", url, queryString);
        XxlJobHelper.log("EVO bet log url: {}, queryString : {}", url, queryString);
        ResponseEntity<String> jsonResult = restTemplate.exchange(URI.create(url + "?" + queryString), HttpMethod.GET, entity, String.class);
        JSONObject jsonBody;
        try {
            jsonBody = JSONObject.parseObject(jsonResult.getBody());
        } catch (Exception e) {
            log.info("fetch EVO fail : {} ", jsonResult.getBody());
            XxlJobHelper.log("fetch EVO fail : {} ", jsonResult.getBody());
            throw new RuntimeException("fetch EVO fail", e);
        }
        if (jsonBody.containsKey("data")) {
            JSONArray data = jsonBody.getJSONArray("data");
            if (data.size() == 0)
                return new ArrayList<>();
            List<JSONObject> recordList = (List) data.stream()
                    .map(x -> ((Map) x).get("games"))
                    .flatMap(x -> ((List) x).stream())
                    .collect(Collectors.toList());

            List<EvoBetOrderVO> betOrderVOList = recordList.stream().map(obj -> {
                EvoBetOrderVO evoBetOrderVO = JSONObject.parseObject(obj.toJSONString(), EvoBetOrderVO.class);
                return evoBetOrderVO;
            }).collect(Collectors.toList());
            return betOrderVOList;
        }
        return new ArrayList<>();
    }

    @Transactional
    public Long saveOrder(List<EvoBetOrderVO> list) {
        AtomicReference<Long> version = new AtomicReference<>(0L);
        list.forEach(vo -> {
            JSONObject betsObj = vo.getParticipants().get(0).getJSONArray("bets").getJSONObject(0);
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, vo.getParticipants().get(0).getString("playerId"));
            if (platformGameMember == null) {
                return;
            }
            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(betsObj.getString("transactionId"));
            betOrder.setTransactionSerial(betsObj.getString("transactionId"));
            LocalDateTime wagersTime = betsObj.getObject("placedOn", LocalDateTime.class);
            betOrder.setTransactionTime(wagersTime);

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

            betOrder.setGameId(vo.getTable().getString("id"));
            betOrder.setGameName(vo.getTable().getString("name"));
            betOrder.setGameCategoryCode("live");

            betOrder.setValidBetAmount(vo.getWager());
            betOrder.setBetAmount(vo.getWager());
            betOrder.setSettle(vo.getPayout());

            betOrder.setSettleTime(vo.getSettledAt());
            betOrder.setWinLoss(vo.getPayout().subtract(vo.getWager()));
            betOrder.setBetState(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue());
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(PLATFORM_CODE);
            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

        });
        return version.get();
    }

}
