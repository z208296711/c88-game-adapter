package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.constants.SABAConstants;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.pojo.entity.SabaVersions;
import com.c88.game.adapter.repository.IBetOrderRepository;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.ISabaVersionsService;
import com.c88.game.adapter.service.third.vo.SABA.BetDetailsItem;
import com.c88.game.adapter.service.third.vo.SABA.BetNumberDetailsItem;
import com.c88.game.adapter.service.third.vo.SABA.BetVirtualSportDetailsItem;
import com.c88.game.adapter.service.third.vo.SABA.SABABetOrderVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Slf4j
@Component
@RequiredArgsConstructor
public class SABAGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IBetOrderRepository iBetOrderRepository;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    private final IPlatformService iplatformService;

    private final ISabaVersionsService iSabaVersionsService;

    private final IBetOrderService iBetOrderService;

    private static final String PLATFORM_CODE = "SABA";

    public static final Integer SUCCESS_CODE = 0;

    public static final Integer STATUS_PENDING = 2;

    private String apiUrl;

    private String vendorId;

    private String operatorId;

    private String prefix;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
//        this.gameUrl = apiParameter.getGameUrl();
        this.apiUrl = apiParameter.getApiUrl();
        this.vendorId = apiParameter.getOthers();
        this.operatorId = apiParameter.getApiId();
        this.prefix = apiParameter.getPrefix();
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
        long token = System.currentTimeMillis();
        redisTemplate.opsForValue().set(RedisKey.TOKEN_VALID_BY_SBA + ":" + token, username, 10, TimeUnit.SECONDS);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("vendor_member_id", this.getUsername(username) + "_test");
        params.add("vendor_id", operatorId);
        params.add("platform", param.get("gameplat") == null ? 2 : param.get("gameplat")); //1:桌机, 2:手机 h5, 3:手机纯文字

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

        String url = apiUrl + SABAConstants.GAME_URL;
        String response = restTemplate.postForObject(url, request, String.class);

        JSONObject result = JSON.parseObject(Objects.requireNonNull(response));
        if (result.getInteger("error_code") == 0) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(result.getString("Data"));
//            uriComponentsBuilder.queryParam("lang", "vn");
//            if(param.get("type").equals("esports")) {
//                if(param.get("gameplat").equals("mobile"))
//                    uriComponentsBuilder.queryParam("types", "esports");
//                else
//                    uriComponentsBuilder.queryParam("game", "esports");
//            }
            return Result.success(uriComponentsBuilder.toUriString());
        }
        return Result.failed(result.getString("message"));
//        return Result.success();
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        String url = apiUrl + SABAConstants.REGISTER_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

        params.add("vendor_id", operatorId);
        params.add("Vendor_Member_ID", this.getUsername(username) + "_test");
        params.add("OperatorId", vendorId);
        params.add("UserName", this.getUsername(username) + "_test");
        params.add("OddsType", SABAConstants.CHINA_ODDS);
        params.add("Currency", SABAConstants.CURRENCY_TEST);
        params.add("MaxTransfer", 7203685477l);
        params.add("MinTransfer", 10);

        String response = restTemplate.postForObject(url, request, String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        String status = jsonResult.getString("error_code");
//
        if (!SABAConstants.RESPONSE_SUCCESS.equals(status)) {
            log.info("saba_register_fail:{}", jsonResult.getString("message"));
            return Result.failed(jsonResult.getString("message"));
        } else {
            iPlatformGameMemberService.save(
                    PlatformGameMember.builder()
                            .memberId(memberId)
                            .username(this.getUsername(username) + "_test")
                            .platformId(platform.getId())
                            .code(SABAConstants.PLATFORM_CODE)
                            .build()
            );
            return Result.success(username);
        }
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        String url = apiUrl + SABAConstants.BALANCE_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

        params.add("vendor_id", operatorId);
        params.add("vendor_member_ids", this.getUsername(username) + "_test");
        params.add("wallet_id", 1);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            JSONObject jsonResult = JSON.parseObject(response);
            log.info("SABA_balance:{}", jsonResult);
            if (SUCCESS_CODE.equals(jsonResult.getInteger("error_code"))) {
                JSONArray data = (JSONArray) jsonResult.getJSONArray("Data");
                JSONObject wallent = (JSONObject) data.get(0);
                if (SUCCESS_CODE.equals(wallent.getInteger("error_code"))) {
                    log.info("amount:{}", wallent.getBigDecimal("balance"));
                    return Result.success(wallent.getBigDecimal("balance"));
                } else {
                    return Result.failed(wallent.getString("error_code"));
                }
            }
            return Result.failed(jsonResult.getString("Message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, SABAConstants.TRANSFER_IN);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, SABAConstants.TRANSFER_OUT);
    }

    public Result<TransferStateVO> transfer(String username, BigDecimal amount, String transactionNo, Integer direction) {
        String url = apiUrl + SABAConstants.TRANSFER_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

        params.add("vendor_id", operatorId);
        params.add("vendor_member_id", this.getUsername(username) + "_test");
        params.add("vendor_trans_id", transactionNo);
        params.add("amount", amount);
        params.add("currency", SABAConstants.CURRENCY_TEST);
        params.add("direction", direction);
        params.add("wallet_id", 1);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            JSONObject jsonResult = JSON.parseObject(response);

            log.info("SABA_transfer:{}", jsonResult);
            if (SUCCESS_CODE.equals(jsonResult.getInteger("error_code"))) {
                JSONObject data = (JSONObject) jsonResult.get("Data");
                TransferStateVO stateVO = new TransferStateVO();
                stateVO.setBalance(data.getBigDecimal("after_amount"));
                stateVO.setState(AdapterTransferStateEnum.SUCCESS);
                return Result.success(stateVO);
            } else {
                if (1 == jsonResult.getInteger("error_code")) {
                    JSONObject data = (JSONObject) jsonResult.get("Data");
                    int status = data.getInteger("status");
                    if (status == STATUS_PENDING) {
                        TransferStateVO stateVO = new TransferStateVO();
                        stateVO.setState(AdapterTransferStateEnum.IN_PROGRESS);
                        return Result.success(stateVO);
                    }
                    TransferStateVO stateVO = new TransferStateVO();
                    stateVO.setState(AdapterTransferStateEnum.FAIL);
                    return Result.success(stateVO);
                } else {
                    TransferStateVO stateVO = new TransferStateVO();
                    stateVO.setState(AdapterTransferStateEnum.FAIL);
                    return Result.success(stateVO);
                }
            }
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        String url = apiUrl + SABAConstants.CHECK_TRANSFER_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

        params.add("vendor_id", operatorId);
        params.add("vendor_trans_id", orderId);
        params.add("wallet_id", 1);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            JSONObject jsonResult = JSON.parseObject(response);
            log.info("SABA_check_transfer:{}", jsonResult);
            int status = jsonResult.getInteger("status");
            if (status == 0) {
                return Result.success("TRANSFER_SUCCESS");
            }
            if (status == 1) {
                return Result.success("TRANSFER_FAIL");
            }
            if (status == 3) {
                return Result.success("TRANSFER_PROCESSING");
            }
            if (status == 10) {
                return Result.success("SABA_MAINTAINING");
            }

            return Result.success("TRANSFER_PROCESSING");
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
        SABABetOrderVO dataList = this.fetchBetOrder(Long.valueOf(param.getOrDefault("version", "0")));
        this.saveOrder(dataList);
    }

    /**
     * 拉取注單
     */
    @Override
    @Transactional
    public void doFetchBetOrderAction() {
        SabaVersions lastVersion = iSabaVersionsService.getLastVersion();
        if (lastVersion == null) {
            lastVersion = new SabaVersions();
            lastVersion.setVersion(0L);
        }
//        SabaVersions lastVersion = new SabaVersions();
//            lastVersion.setVersion(0L);
//        do {
        try {
            SABABetOrderVO vo = this.fetchBetOrder(lastVersion.getVersion());
            if (!Objects.isNull(vo)) {
//                    List dataList = vo.getBetDetails();
                Long newVersion = this.saveOrder(vo);
                lastVersion.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_DONE.getValue());
                iSabaVersionsService.saveOrUpdate(lastVersion);
                //新version紀錄
                if (newVersion != null && newVersion != 0) {
                    SabaVersions sabaVersions = new SabaVersions();
                    sabaVersions.setVersion(newVersion);
                    sabaVersions.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue());
                    iSabaVersionsService.save(sabaVersions);
                }
//                    if (dataList.size() < 1000) {
//                        break;
//                    }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SabaVersions sabaVersions = new SabaVersions();
            sabaVersions.setVersion(lastVersion.getVersion());
            sabaVersions.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_ERROR.getValue());
            iSabaVersionsService.save(sabaVersions);
        }
//            return;
//        } while (true);
    }

    @Override
    public <T> List<T> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    public SABABetOrderVO fetchBetOrder(Long version) {
        String url = apiUrl + SABAConstants.BET_DETAIL_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

        params.add("vendor_id", operatorId);
        params.add("version_key", version);
        String response = restTemplate.postForObject(url, request, String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        log.info("SABA_betDetail:{}", jsonResult);
        if (SUCCESS_CODE.equals(jsonResult.getInteger("error_code"))) {
            return jsonResult.getJSONObject("Data").toJavaObject(SABABetOrderVO.class);
        }
        throw new RuntimeException(jsonResult.getString("Message"));
    }

    private String getOriginUsername(String username) {
        return username.substring(3);
    }

    @Transactional
    public Long saveOrder(SABABetOrderVO vo) {

        //waiting(等待中): 我们交易员因为可能因为赔率的转换等因素，还未接受这张注单。
        //running(进行中): 此注单还没有结算。（注单还没有结算的状态有可能是这场比赛还没有结算之类的情形.）
        //void (作废): 在注单为 running 的状态下，玩家下注注金返回。原因可能为我们交易员对此场赛事有些疑虑。可与我们联系询问发生什么状况。
        //refund(退款): 在注单为 running 的状态下，玩家下注注金返回。原因有可能是赛事取消或发生什么意外。
        //reject(已取消): 在注单为 waiting 的状态下，玩家下注注金返回。可能状况很多。
        //lose(输): 此注单已结算且玩家输了此注单。
        //won(赢): 此注单已结算且玩家赢了此注单。
        //draw(和局): 此注单已结算且此注单为和局。
        //half won(半赢): 此注单已结算且玩家赢了下注额一半。
        //half lose(半输): 此注单已结算且玩家输了下注额一半。

        Set<String> winLoseStatusSet = Set.of("won", "lose", "draw", "half won", "half lose");
        Set<String> dangerStatusSet = Set.of("void", "refund", "reject");

        Long version = vo.getLastVersionKey();//new AtomicReference<>(0L);

        List<BetDetailsItem> betDetails = vo.getBetDetails();
        List<BetNumberDetailsItem> betNumberDetails = vo.getBetNumberDetails();
        List<BetVirtualSportDetailsItem> betVirtualSportDetails = vo.getBetVirtualSportDetails();

//        for (BetDetailsItem bet : betDetails){
        Optional.ofNullable(betDetails).orElse(Collections.emptyList()).forEach(bet -> {
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, bet.getVendorMemberId());
//            log.info("saba_platform:{}, memberid:{}, platformGameMember:{}", PLATFORM_CODE, bet.getVendorMemberId(), platformGameMember);
            if (platformGameMember == null) {
//                break;
                return;
            }

            if (bet.getTicketStatus().equals("waiting")) {
//                break;
                return;
            }

            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            if (dangerStatusSet.contains(bet.getTicketStatus())) {
                status = BetOrderTypeEnum.BET_STATUS_CANCELED.getValue();
            }

            BigDecimal winLoss = BigDecimal.ZERO; //總輸贏
            BigDecimal settle = BigDecimal.ZERO; //總派彩
            BigDecimal validBet = BigDecimal.ZERO; //有效投注
            if (winLoseStatusSet.contains(bet.getTicketStatus())) {
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
                winLoss = new BigDecimal(bet.getWinlostAmount());
                settle = new BigDecimal(bet.getStake()).add(winLoss);
                validBet = this.getValidBet(winLoss, new BigDecimal(bet.getStake()));
            }

//            DateTimeFormatter dateTimeFormatterS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S");
            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(bet.getTransId());
            betOrder.setTransactionSerial(String.valueOf(bet.getTransId())); //TODO
            betOrder.setTransactionTime(LocalDateTime.parse(bet.getTransactionTime()).plusHours(SABAConstants.TIMEZONE));

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());
            betOrder.setPlatformCode(platformGameMember.getCode());

            betOrder.setGameId("S1");
            betOrder.setGameName("sport"); // 只有電競&體育兩種

            betOrder.setGameCategoryCode("sport");
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(new BigDecimal(bet.getStake()));

            if (status.equals(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betOrder.setSettle(settle);
//                DateTimeFormatter dateTimeFormatterSSS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                betOrder.setSettleTime(LocalDateTime.parse(bet.getSettlementTime()).plusHours(SABAConstants.TIMEZONE));
            }

            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

//            version.set(Long.parseLong(bet.getVersionKey()));
//        };
        });

        Optional.ofNullable(betNumberDetails).orElse(Collections.emptyList()).forEach(bet -> {
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, bet.getVendorMemberId());
            if (platformGameMember == null) {
                return;
            }

            if (bet.getTicketStatus().equals("waiting")) {
                return;
            }

            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            if (dangerStatusSet.contains(bet.getTicketStatus())) {
                status = BetOrderTypeEnum.BET_STATUS_CANCELED.getValue();
            }

            BigDecimal winLoss = BigDecimal.ZERO; //總輸贏
            BigDecimal settle = BigDecimal.ZERO; //總派彩
            BigDecimal validBet = BigDecimal.ZERO; //有效投注
            if (winLoseStatusSet.contains(bet.getTicketStatus())) {
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
                settle = new BigDecimal(bet.getWinlostAmount());
                validBet = this.getValidBet(winLoss, new BigDecimal(bet.getStake()));
                winLoss = settle.subtract(validBet);
            }

//            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S");
            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(bet.getTransId());
            betOrder.setTransactionSerial(String.valueOf(bet.getTransId())); //TODO
            betOrder.setTransactionTime(LocalDateTime.parse(bet.getTransactionTime()).plusHours(SABAConstants.TIMEZONE));

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());
            betOrder.setPlatformCode(platformGameMember.getCode());

            betOrder.setGameId("S1");
            betOrder.setGameName("numberOne"); // 只有電競&體育兩種

//            betOrder.setGameCategoryCode(bet.getBetType());
            betOrder.setGameCategoryCode("sport");
            betOrder.setValidBetAmount(new BigDecimal(bet.getStake()));
            betOrder.setBetAmount(new BigDecimal(bet.getStake()));

            if (status.equals(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betOrder.setSettle(settle);
                betOrder.setSettleTime(LocalDateTime.parse(bet.getWinlostDatetime()).plusHours(SABAConstants.TIMEZONE));
            }

            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

//            version.set(Long.parseLong(bet.getVersionKey()));
        });

        Optional.ofNullable(betVirtualSportDetails).orElse(Collections.emptyList()).forEach(bet -> {
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, bet.getVendorMemberId());
            if (platformGameMember == null) {
                return;
            }

            if (bet.getTicketStatus().equals("waiting")) {
                return;
            }

            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            if (dangerStatusSet.contains(bet.getTicketStatus())) {
                status = BetOrderTypeEnum.BET_STATUS_CANCELED.getValue();
            }

            BigDecimal winLoss = BigDecimal.ZERO; //總輸贏
            BigDecimal settle = BigDecimal.ZERO; //總派彩
            BigDecimal validBet = BigDecimal.ZERO; //有效投注
            if (winLoseStatusSet.contains(bet.getTicketStatus())) {
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
                settle = new BigDecimal(bet.getWinlostAmount());
                validBet = this.getValidBet(winLoss, new BigDecimal(bet.getStake()));
                winLoss = settle.subtract(validBet);
            }

//            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S");
            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(bet.getTransId());
            betOrder.setTransactionSerial(String.valueOf(bet.getTransId())); //TODO
            betOrder.setTransactionTime(LocalDateTime.parse(bet.getTransactionTime()).plusHours(SABAConstants.TIMEZONE));

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());
            betOrder.setPlatformCode(platformGameMember.getCode());

            betOrder.setGameId("S1");
            betOrder.setGameName("virtualSport"); // 只有電競&體育兩種

//            betOrder.setGameCategoryCode(bet.getBetType());
            betOrder.setGameCategoryCode("sport");
            betOrder.setValidBetAmount(new BigDecimal(bet.getStake()));
            betOrder.setBetAmount(new BigDecimal(bet.getStake()));

            if (status.equals(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betOrder.setSettle(settle);
                betOrder.setSettleTime(LocalDateTime.parse(bet.getWinlostDatetime()).plusHours(SABAConstants.TIMEZONE));
            }

            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(PLATFORM_CODE);

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

//            version.set(Long.parseLong(bet.getVersionKey()));
        });

        return version;

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
