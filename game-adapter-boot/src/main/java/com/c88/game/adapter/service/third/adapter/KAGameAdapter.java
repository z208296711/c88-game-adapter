package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.*;
import com.c88.game.adapter.service.*;
import com.c88.game.adapter.service.third.vo.KaBetOrderVO;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.c88.common.core.result.Result.isSuccess;
import static com.c88.common.core.util.GameUtil.calcHmacSha256;

@Slf4j
@Component
@RequiredArgsConstructor
public class KAGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    private final IPlatformService iplatformService;

    private final IFetchRecordService iFetchRecordService;
    private final IRetryFetchOrder retryFetchOrderImpl;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final String PLATFORM_CODE = "KA";

    public static final Integer SUCCESS_CODE = 0;

    private String accessKey;

    private String secretKey;

    private String gameUrl;

    private String apiUrl;

    private String prefix;

    private String suffix;

    private String partnerName;

    private final IPlatformGameService iPlatformGameService;

    /**
     * 遊戲類型轉換
     */
    private Map<String, String> gameCategoryMapConverter = Map.of(
            "fish", "slot",
            "slots", "slot"
    );

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.accessKey = apiParameter.getApiId();
        this.secretKey = apiParameter.getApiKey();
        this.gameUrl = apiParameter.getGameUrl();
        this.apiUrl = apiParameter.getApiUrl();
        this.partnerName = apiParameter.getOthers();
        this.prefix = apiParameter.getPrefix();
        this.suffix = apiParameter.getSuffix();
    }

    @Override
    public String getUsername(String username) {
        return prefix + username + suffix;
    }

    @Override
    public String getGamePlatformCode() {
        return PLATFORM_CODE;
    }

    private void printGameList() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl + "/gameList");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partnerName", this.partnerName);
        jsonObject.put("language", "VN");
        jsonObject.put("accessKey", this.accessKey);
        jsonObject.put("randomId", new Random().nextInt());
        String json = JSON.toJSONString(jsonObject);
        uriComponentsBuilder.queryParam("hash", calcHmacSha256(this.secretKey, json));
        log.info("KA printGameList url: {}, jsonObject : {}", uriComponentsBuilder.toUriString(), json);

        try {
            String kaResponse = restTemplate.postForObject(uriComponentsBuilder.toUriString(), jsonObject, String.class);
            JSONObject jsonResult = JSON.parseObject(kaResponse);
            JSONArray games = (JSONArray) jsonResult.get("games");
            StringBuilder sb = new StringBuilder();
            games.forEach(g -> {
                JSONObject game = (JSONObject) g;
                sb.append("gameId:" + game.get("gameId") +
                        ", gameType:" + game.get("gameType") + ", icon:" +
                        game.get("iconURLPrefix").toString().replaceAll("\\u003d", "=").replaceAll("\\u0026", "&") + "&type=square");
                sb.append(System.lineSeparator());
            });
            log.info(sb.toString());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {

        long token = System.currentTimeMillis();
        redisTemplate.opsForValue().set(RedisKey.TOKEN_VALID_BY_KA + ":" + token, username, 10, TimeUnit.SECONDS);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.gameUrl);
        uriComponentsBuilder.queryParam("g", param.get("GameId"));// GAME_NAME
        uriComponentsBuilder.queryParam("p", this.partnerName);
        uriComponentsBuilder.queryParam("u", this.getUsername(username));
        uriComponentsBuilder.queryParam("cr", "PHP");
        uriComponentsBuilder.queryParam("loc", "en");
        uriComponentsBuilder.queryParam("t", token);
        uriComponentsBuilder.queryParam("ak", this.accessKey);
        log.info(uriComponentsBuilder.toUriString());
        return Result.success(uriComponentsBuilder.toUriString());
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        Result<TransferStateVO> result = transferIntoThird(username, BigDecimal.ZERO, "");// deposit 會自動建立 user wallet

        if (!isSuccess(result)) {
            return Result.failed(result.getMsg());
        }

        String platformUsername = this.getUsername(username);
        PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, platformUsername);
        if (platformGameMember == null) {
            iPlatformGameMemberService.save(
                    PlatformGameMember.builder()
                            .memberId(memberId)
                            .username(platformUsername)
                            .platformId(platform.getId())
                            .code(PLATFORM_CODE)
                            .build()
            );
        }

        return Result.success(username);
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl + "/wallet/balance");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.getUsername(username));
        jsonObject.put("partnerName", this.partnerName);
        jsonObject.put("currency", "PHP");
        jsonObject.put("accessKey", this.accessKey);
        String json = JSON.toJSONString(jsonObject);
        uriComponentsBuilder.queryParam("hash", calcHmacSha256(this.secretKey, json));
        log.info("KA findMemberBalance url: {}, jsonObject : {}", uriComponentsBuilder.toUriString(), json);

        try {
            String kaResponse = restTemplate.postForObject(uriComponentsBuilder.toUriString(), jsonObject, String.class);
            log.info("KA findMemberBalance result: {}", kaResponse);
            JSONObject jsonResult = JSON.parseObject(kaResponse);
            Integer code = jsonResult.getInteger("statusCode");
            if (SUCCESS_CODE.equals(code)) {
                BigDecimal balance = jsonResult.getBigDecimal("balance");
                return Result.success(balance.divide(HUNDRED, 2, RoundingMode.HALF_UP));// KA金額都以分为单位，取得的金額要除100
            }
            return Result.failed(jsonResult.getString("status"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl + "/wallet/deposit");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.getUsername(username));
            jsonObject.put("partnerName", this.partnerName);
            jsonObject.put("currency", "PHP");
            jsonObject.put("accessKey", this.accessKey);
            jsonObject.put("depositAmount", amount.multiply(HUNDRED));// KA金額都以分为单位，傳入的金額要乘100
            jsonObject.put("externalTransactionId", transactionNo);
            String json = JSON.toJSONString(jsonObject);
            uriComponentsBuilder.queryParam("hash", calcHmacSha256(this.secretKey, json));
            log.info("KA transfer in url: {}, jsonObject : {}", uriComponentsBuilder.toUriString(), json);

            String kaResponse = restTemplate.postForObject(uriComponentsBuilder.toUriString(), jsonObject, String.class);
            log.info("KA transfer in result: {}", kaResponse);
            JSONObject jsonResult = JSON.parseObject(kaResponse);
            Integer code = jsonResult.getInteger("statusCode");
            if (SUCCESS_CODE.equals(code)) {
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.SUCCESS)
                                .build()
                );
            }
        } catch (Exception ex) {
            log.info("KA transferIn fail : {}", ex.getMessage());
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
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl + "/wallet/withdraw");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.getUsername(username));
            jsonObject.put("partnerName", this.partnerName);
            jsonObject.put("currency", "PHP");
            jsonObject.put("accessKey", this.accessKey);
            jsonObject.put("withdrawalAmount", amount.multiply(HUNDRED));
            jsonObject.put("externalTransactionId", transactionNo);
            String json = JSON.toJSONString(jsonObject);
            uriComponentsBuilder.queryParam("hash", calcHmacSha256(this.secretKey, json));
            log.info("KA transfer out url: {}, jsonObject : {}", uriComponentsBuilder.toUriString(), json);

            String kaResponse = restTemplate.postForObject(uriComponentsBuilder.toUriString(), jsonObject, String.class);
            log.info("KA transfer out result: {}", kaResponse);
            JSONObject jsonResult = JSON.parseObject(kaResponse);
            Integer code = jsonResult.getInteger("statusCode");
            if (SUCCESS_CODE.equals(code)) {
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.SUCCESS)
                                .build()
                );
            }

        } catch (Exception ex) {
            log.info("KA transferOut fail : {}", ex.getMessage());
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
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl + "/wallet/checkTransaction");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.getUsername(username));
            jsonObject.put("partnerName", this.partnerName);
            jsonObject.put("currency", "PHP");
            jsonObject.put("accessKey", this.accessKey);
            jsonObject.put("walletTransactionId", orderId);
            String json = JSON.toJSONString(jsonObject);
            uriComponentsBuilder.queryParam("hash", calcHmacSha256(this.secretKey, json));
            log.info("KA findTicketStatus url: {}, jsonObject : {}", uriComponentsBuilder.toUriString(), json);

            String kaResponse = restTemplate.postForObject(uriComponentsBuilder.toUriString(), jsonObject, String.class);
            log.info("KA findTicketStatus kaResponse: {}", kaResponse);
            JSONObject jsonResult = JSON.parseObject(kaResponse);
            Integer code = jsonResult.getInteger("statusCode");
            if (SUCCESS_CODE.equals(code)) {
                return Result.success("TRANSFER_SUCCESS");
            } else if (code == 5) {
                return Result.success("TRANSFER_NOT_FOUND");
            } else {
                return Result.success("TRANSFER_FAIL");
            }
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
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
        List<KaBetOrderVO> dataList = this.fetchBetOrder(startDateTime, endDateTime);
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
            List<KaBetOrderVO> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("KA_exception:{}",ex.toString());
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
                if(CollectionUtils.isNotEmpty((List<KaBetOrderVO>)retryFetchOrder)) {
                    this.saveOrder((List<KaBetOrderVO>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    public List<KaBetOrderVO> fetchBetOrder(LocalDateTime start, LocalDateTime end) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl + "/playerReport");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partnerName", this.partnerName);
        jsonObject.put("currency", "PHP");
        jsonObject.put("accessKey", this.accessKey);
        jsonObject.put("randomId", new Random().nextInt());
        jsonObject.put("from", dateTimeFormatter.format(start));
        jsonObject.put("to", dateTimeFormatter.format(end));
        String json = JSON.toJSONString(jsonObject);
        uriComponentsBuilder.queryParam("hash", calcHmacSha256(this.secretKey, json));
        log.info("KA fetchBetOrder url: {}, jsonObject : {}", uriComponentsBuilder.toUriString(), json);

        String kaResponse = restTemplate.postForObject(uriComponentsBuilder.toUriString(), jsonObject, String.class);
        log.info("KA fetchBetOrder kaResponse: {}", kaResponse);
        JSONObject jsonResult = JSON.parseObject(kaResponse);
        Integer code = jsonResult.getInteger("statusCode");
        if (!SUCCESS_CODE.equals(code)) {
            throw new RuntimeException(jsonResult.getString("status"));
        }
        return jsonResult.containsKey("spinReport") ? jsonResult.getJSONArray("spinReport").toJavaList(KaBetOrderVO.class) : new ArrayList<>();
    }

    private String getOriginUsername(String username) {
        return username.substring(3, username.length() - 3);
    }

    @Transactional
    public Long saveOrder(List<KaBetOrderVO> list) {

        AtomicReference<Long> version = new AtomicReference<>(0L);
        list.forEach(vo -> {

            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, vo.getPlayerId());
            if (platformGameMember == null) {
                return;
            }

            BigDecimal winLoss = BigDecimal.ZERO;
            BigDecimal validBet = BigDecimal.ZERO;
            BigDecimal settle = BigDecimal.ZERO;// 派彩
            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            if (vo.getCashWon() >= 0) {
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
                settle = BigDecimal.valueOf(vo.getCashWon()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                validBet = BigDecimal.valueOf(vo.getFreePlayed()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                winLoss = settle.subtract(validBet);
            }

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getTransactionId() + vo.getRound());
            betOrder.setTransactionSerial(vo.getTransactionId());
            betOrder.setTransactionTime(vo.getSpinDate());

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

            betOrder.setGameId(vo.getGameName());
            betOrder.setGameName(vo.getGameName());

            betOrder.setGameCategoryCode(gameCategoryMapConverter.getOrDefault(vo.getGameType(), vo.getGameType()));
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(validBet);

            if (status.equals(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betOrder.setSettle(settle);
                betOrder.setSettleTime(vo.getSpinDate());
            }

            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(PLATFORM_CODE);

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

        });
        return version.get();
    }
}
