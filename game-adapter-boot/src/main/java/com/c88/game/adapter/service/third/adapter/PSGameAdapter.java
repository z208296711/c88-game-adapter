package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.constants.PSConstants;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.FetchRecord;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.repository.IBetOrderRepository;
import com.c88.game.adapter.service.*;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.PS.PSBetOrderVo;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.utils.PSGenerateTokenUtils;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Slf4j
@Component
@RequiredArgsConstructor
public class PSGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IFetchRecordService iFetchRecordService;

    private final IBetOrderRepository iBetOrderRepository;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    private final IPlatformService iplatformService;

    private final IPlatformGameService iPlatformGameService;

    private final ISabaVersionsService iSabaVersionsService;

    private final IBetOrderService iBetOrderService;
    private final IRetryFetchOrder retryFetchOrderImpl;

    public static final Integer SUCCESS_CODE = 0;

    public static final Integer STATUS_PENDING = 2;

    private String apiUrl;
    private String prefix;
    private String agentCode;
    private String agentKey;
    private String secretKey;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PSConstants.PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PSConstants.PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.apiUrl = apiParameter.getApiUrl();
        this.prefix = apiParameter.getPrefix();
        this.agentKey = apiParameter.getApiKey();
        this.secretKey = apiParameter.getOthers();
        this.agentCode = apiParameter.getApiId();
    }

    @Override
    public String getUsername(String username) {
        return prefix + username;
    }

    @Override
    public String getGamePlatformCode() {
        return PSConstants.PLATFORM_CODE;
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {
        long timeUnit = System.currentTimeMillis();
        redisTemplate.opsForValue().set(RedisKey.TOKEN_VALID_BY_PS + ":" + timeUnit, username, 10, TimeUnit.SECONDS);
        HttpHeaders headers = getHttpHeaders();
        String url = apiUrl + PSConstants.LOGIN_URL;

        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("userCode", getUsername(username))
                .queryParam("locale", "en")
//                .queryParam("view",param.get("GameId"))
                .build().toString();
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class);
        JSONObject result = exchange.getBody();

        if (StringUtils.isNotBlank(result.getString("loginUrl"))) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(result.getString("loginUrl"));
            return Result.success(uriComponentsBuilder.toUriString());
        }
        log.info("PS game error msg:{}", result.toString());
        return Result.failed();
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        String url = apiUrl + PSConstants.REGISTER_URL;
        HttpHeaders headers = getHttpHeaders();

        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("agentCode", agentCode)
                .queryParam("loginId", getUsername(username))
                .build().toString();
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class);
        JSONObject result = exchange.getBody();

        if (StringUtils.isBlank(result.getString("loginId"))) {
            log.info("ps_register_fail:{}", result.toString());
            return Result.failed();
        } else {
            if (Objects.isNull(iPlatformGameMemberService.lambdaQuery()
                    .eq(PlatformGameMember::getPlatformId, platform.getId())
                    .eq(PlatformGameMember::getMemberId, memberId).one())) {
                iPlatformGameMemberService.save(
                        PlatformGameMember.builder()
                                .memberId(memberId)
                                .username(this.getUsername(username))
                                .platformId(platform.getId())
                                .code(PSConstants.PLATFORM_CODE)
                                .build()
                );
            }
            return Result.success(username);
        }
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        String url = apiUrl + PSConstants.BALANCE_URL;
        HttpHeaders headers = getHttpHeaders();

        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("userCode", getUsername(username))
                .build().toString();
        try {
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class);
            JSONObject result = exchange.getBody();

            if (Objects.nonNull(result.getBigDecimal("availableBalance"))) {
                return Result.success(result.getBigDecimal("availableBalance"));
            } else {
                log.info("PS game error msg:{}", result.toString());
                return Result.success(BigDecimal.ZERO);
            }
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, PSConstants.TRANSFER_IN);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, PSConstants.TRANSFER_OUT);
    }

    public Result<TransferStateVO> transfer(String username, BigDecimal amount, String transactionNo, String URL) {
        String url = apiUrl + URL;
        HttpHeaders headers = getHttpHeaders();

        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("userCode", getUsername(username))
                .queryParam("amount", amount)
                .queryParam("transactionId", transactionNo.replaceAll("-", ""))
                .build().toString();
        try {
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class);
            JSONObject result = exchange.getBody();

            if (Objects.nonNull(result.getBigDecimal("availableBalance"))) {
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.SUCCESS)
                                .build()
                );
            } else {
                log.info("PS game error msg:{}", result.toString());
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.FAIL)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("PS transfer Exception : {}", ExceptionUtil.stacktraceToString(e));
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
        String url = apiUrl + PSConstants.CHECK_TRANSFER_URL;
        HttpHeaders headers = getHttpHeaders();
        orderId = orderId.replaceAll("-", "");
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("transactionId", orderId)
                .build().toString();

        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class);
        JSONObject result = exchange.getBody();
        if (result.getString("status").equals(PSConstants.SUCCESS)) {
            return Result.success("TRANSFER_SUCCESS");
        }
        if (result.getString("status").equals(PSConstants.FAILED)) {
            return Result.success("TRANSFER_FAIL");
        }
        return Result.success("TRANSFER_PROCESSING");

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
        List<PSBetOrderVo> dataList = fetchBetOrder(startDateTime, endDateTime);
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
            List<PSBetOrderVo> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("PS_exception:{}",ex.toString());
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
                if(CollectionUtils.isNotEmpty((List<PSBetOrderVo>)retryFetchOrder)) {
                    this.saveOrder((List<PSBetOrderVo>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    @Override
    public List<PSBetOrderVo> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        String url = apiUrl + PSConstants.BET_DETAIL_URL;
        HttpHeaders headers = getHttpHeaders();
        String startDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(
                startTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("GMT-4")).toLocalDateTime());
        String endDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(
                endTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("GMT-4")).toLocalDateTime());
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("dateFrom", startDate)
                .queryParam("dateTo", endDate)
//                .queryParam("settle", 1)
                .queryParam("filterBy", "update_date")
                .build().toString();
        log.info("request:PS getBetHistoryResult ,startTime={},endTime={}", startDate, endDate);
        ResponseEntity<JSONArray> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, JSONArray.class);
        JSONArray result = exchange.getBody();
        log.info("PS betOrder result msg:{}", result.toString());
        List<PSBetOrderVo> betOrders = new ArrayList<>();
        if (PSConstants.RESPONSE_SUCCESS.equals(String.valueOf(exchange.getStatusCodeValue())) && !result.isEmpty()) {
            betOrders = result.stream()
                    .map(o -> JSONObject.parseObject(JSON.toJSONString(o)))
                    .map(o -> o.toJavaObject(PSBetOrderVo.class)).collect(Collectors.toList());
        }
        return betOrders;

    }

    private String getOriginUsername(String username) {
        return username.substring(3);
    }

    @Transactional
    public Long saveOrder(List<PSBetOrderVo> list) {
        AtomicReference<Long> version = new AtomicReference<>(0L);
        list.forEach(vo -> {

            String gameType = "sport";
            String gameId = "v1";
            if ("E Sports".equals(vo.getSport())) {
                gameType = "esport";
                gameId = "v3";
            }
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(this.getGamePlatformCode(), vo.getLoginId());
            if (platformGameMember == null) {
                return;
            }
            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            Integer settleNote = null;
            if (Objects.nonNull(vo.getCancellationStatus())) {
                settleNote = ReBateStatusEnum.CANCEL.getValue();
                        status = BetOrderTypeEnum.BET_STATUS_CANCELED.getValue();
            }
            BigDecimal winLoss = BigDecimal.ZERO; //總輸贏
            BigDecimal settle = BigDecimal.ZERO; //總派彩
            BigDecimal validBet = BigDecimal.ZERO; //有效投注
            BigDecimal stake = new BigDecimal(vo.getStake()); //
            LocalDateTime settleTime = null;
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (vo.getStatus().equals("SETTLED")) {
                settleTime = LocalDateTime.parse(vo.getSettleDateFm(),df).atZone(ZoneId.of("-4")).withZoneSameInstant(ZoneId.of("Etc/GMT+0")).toLocalDateTime();
                winLoss = new BigDecimal(vo.getWinLoss());
                validBet = this.getValidBet(winLoss, stake);
                settle = winLoss.add(validBet);
                // 派彩= 輸贏 + 投注
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();

            }
            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getLeagueId() + vo.getWagerId() + "");
            betOrder.setTransactionSerial(vo.getLeagueId() + vo.getWagerId() + "");
            betOrder.setTransactionTime(LocalDateTime.parse(vo.getWagerDateFm(), df).atZone(ZoneId.of("-4")).withZoneSameInstant(ZoneId.of("Etc/GMT+0")).toLocalDateTime());

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

            betOrder.setGameId(gameId);
            betOrder.setGameName(gameType);
            betOrder.setPlatformCode(this.getGamePlatformCode());

            betOrder.setGameCategoryCode(gameType);
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(stake);

            betOrder.setSettle(settle);
            betOrder.setSettleTime(settleTime);
            betOrder.setWinLoss(winLoss);

            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(this.getGamePlatformCode());
            betOrder.setSettleNote(settleNote);

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

        });
        return version.get();

    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("userCode", agentCode);
        headers.add("token", PSGenerateTokenUtils.generateToken(agentCode, agentKey, secretKey));
        return headers;
    }

    /**
     * 取有效投注額
     *
     * @param winLoss
     * @param betAmount
     * @return BigDecimal validBet
     */
    public BigDecimal getValidBet(BigDecimal winLoss, BigDecimal betAmount) {
        if (winLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (winLoss.abs().compareTo(betAmount) >= 0) {
            return betAmount;
        }
        return winLoss.abs();
    }
}
