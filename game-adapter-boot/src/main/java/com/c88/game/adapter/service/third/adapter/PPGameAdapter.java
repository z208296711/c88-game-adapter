package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.GameUtil;
import com.c88.game.adapter.constants.PPConstants;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.PPBetInfoStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.PPBetOrderTime;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.pojo.vo.PPGameBetInfoVO;
import com.c88.game.adapter.service.IPPBetOrderTimeService;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.utils.DateUtil;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @description: ppCasinoGame
 * @author: marcoyang
 * @date: 2022/12/19
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class PPGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final IPPBetOrderTimeService ippBetOrderTimeService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private static final String PLATFORM_CODE = "PP";

    public static final Integer SUCCESS_CODE = 0;

    private String partnerKey;

    private String gameUrl;

    private String apiUrl;

    private String prefix;

    private String templateName;

    private String secretKey;

    private String others;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.secretKey = apiParameter.getApiKey();
        this.partnerKey = apiParameter.getApiId();
        this.gameUrl = apiParameter.getGameUrl();
        this.apiUrl = apiParameter.getApiUrl();
        this.others = apiParameter.getOthers();
        this.templateName = "aliceblue";
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
        String url = this.gameUrl + PPConstants.LOGIN_PATH;

        MultiValueMap<String, Object> paraX = new LinkedMultiValueMap<>();
        paraX.add("externalPlayerId", getUsername(username));
        paraX.add("gameId", param.get("GameId"));
        paraX.add("language", "vi");
        paraX.add("secureLogin", this.secretKey);
        JSONObject jsonResult = getJsonObject(url, paraX); //gameURL
        String code = jsonResult.getString("error");
        log.info("PP register getUserName {} result {}", getUsername(username), code);
        if (!PPConstants.RESPONSE_SUCCESS.equals(code)) {
            return Result.failed(jsonResult.getString("description"));
        }
        return Result.success(jsonResult.getString("gameURL"));
    }

    public JSONObject gameList() {
        String url = this.apiUrl + "/getCasinoGames/";
        MultiValueMap<String, Object> paraX = new LinkedMultiValueMap<>();
        paraX.add("secureLogin", this.secretKey);
        return getJsonObject(url, paraX);
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        String url = this.apiUrl + PPConstants.REGISTER_PATH;

        MultiValueMap<String, Object> paraX = new LinkedMultiValueMap<>();
        paraX.add("currency", "VND");
        paraX.add("externalPlayerId", getUsername(username));
        paraX.add("secureLogin", this.secretKey);
        JSONObject jsonResult = getJsonObject(url, paraX);

        String code = jsonResult.getString("error");
        log.info("PP register getUserName {} result {} description {}", getUsername(username), code, jsonResult.getString("description"));
        if (!PPConstants.RESPONSE_SUCCESS.equals(code)) {
            return Result.failed(jsonResult.getString("description"));
        }

        iPlatformGameMemberService.save(
                PlatformGameMember.builder()
                        .memberId(memberId)
                        .username(this.getUsername(username))
                        .platformId(platform.getId())
                        .code(PLATFORM_CODE)
                        .build()
        );

        return Result.success(username);
    }

    private JSONObject getJsonObject(String url, MultiValueMap<String, Object> paraX) {
        String urlQueryPara = doQueryUrl(paraX);
        String hashKey = getHashKey(urlQueryPara, this.others);
        paraX.add("hash", hashKey);
        log.info("PP getJsonObject hashKey {} url {}", hashKey, urlQueryPara);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(paraX, headers);
        String response = null;
        try {
            response = restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            log.info("PP Exception : {}", ExceptionUtil.getRootCauseMessage(e));
            return new JSONObject();
        }
        JSONObject jsonResult = JSON.parseObject(response);
        return jsonResult;
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        String url = apiUrl + PPConstants.BALANCE_PATH;
        MultiValueMap<String, Object> paraX = new LinkedMultiValueMap<>();
        paraX.add("externalPlayerId", getUsername(username));
        paraX.add("secureLogin", this.secretKey);

        try {
            JSONObject jsonResult = getJsonObject(url, paraX);
            log.info("PP  code {} balance {} descript {}", jsonResult.getString("error"), jsonResult.getBigDecimal("balance"), jsonResult.getString("description"));
            String code = jsonResult.getString("error");
            if (PPConstants.RESPONSE_SUCCESS.equals(code)) {
                return Result.success(jsonResult.getBigDecimal("balance"));
            }
            return Result.failed(jsonResult.getString("description"));
        }catch (Exception ex){
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount.negate(), transactionNo);
    }

    private Result<TransferStateVO> transfer(String username, BigDecimal amount, String transactionNo) {
        String url = apiUrl + PPConstants.TRANSFER_PATH;
        MultiValueMap<String, Object> paraX = new LinkedMultiValueMap<>();
        paraX.add("amount", amount);
        paraX.add("externalPlayerId", this.getUsername(username));
        paraX.add("externalTransactionId", transactionNo);
        paraX.add("secureLogin", this.secretKey);

        log.info("PP transfer amount {} externalPlayerId{} externalTransactionId{}", amount, this.getUsername(username), transactionNo);
        JSONObject jsonResult = getJsonObject(url, paraX);
        String code = jsonResult.getString("error");
        if (PPConstants.RESPONSE_SUCCESS.equals(code)) {
            BigDecimal balance = jsonResult.getBigDecimal("balance");
            TransferStateVO stateVO = new TransferStateVO();
            stateVO.setBalance(balance);
            stateVO.setState(AdapterTransferStateEnum.SUCCESS);
            log.info("PP transfer after balance {} ", balance);
            return Result.success(stateVO);
        }
        return Result.failed(jsonResult.getString("description"));
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        String url = apiUrl + PPConstants.TRANSFER_STATUS;
        MultiValueMap<String, Object> paraX = new LinkedMultiValueMap<>();
        paraX.add("externalTransactionId", orderId);
        paraX.add("secureLogin", this.secretKey);
        log.info("PP findTicketStatus orderId {}", orderId);
        JSONObject jsonResult = getJsonObject(url, paraX);

        if (PPConstants.RESPONSE_SUCCESS.equals(jsonResult.getString("error"))) {
            return Result.success(jsonResult.getString("status"));
        }
        return Result.failed(jsonResult.getString("description"));
    }

    @Override
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        Long startTime = ZonedDateTime.of(startDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
        log.info("PP manualBetOrder start {}", startTime);
        doFetchOrderInfo(startTime);
        log.info("PP manualBetOrder end {}", startTime);
    }

    @Override
    public void doFetchBetOrderAction() {
        PPBetOrderTime lastExecutor = ippBetOrderTimeService.getLastVersion();
        lastExecutor = Optional.ofNullable(lastExecutor)
                .orElseGet(() -> {
                    PPBetOrderTime pp = new PPBetOrderTime();
                    pp.setStartTime(LocalDateTime.now().minusMinutes(30));
                    return pp;
                });
        try {
            Long lastTime = toUnixMilliTime(lastExecutor.getStartTime());
            log.info("PP autoBetOrder start {}", lastTime);
            doFetchOrderInfo(lastTime);
            log.info("PP autoBetOrder end {}", lastTime);
            lastExecutor.setEndTime(LocalDateTime.now());
            lastExecutor.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_DONE.getValue());
        } catch (Exception e) {
            lastExecutor.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_ERROR.getValue());

        } finally {
            ippBetOrderTimeService.saveOrUpdate(lastExecutor);
        }
    }

    @Override
    public <T> List<T> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    public String getHashKey(String url, String gameKey) {
        return GameUtil.getMD5(url + gameKey);
    }

    private UriComponents setHash(UriComponents uriComponentsBase, String hashKey) {
        return UriComponentsBuilder.fromUriString(uriComponentsBase.toUriString())
                .queryParam("hash", hashKey)
                .build();
    }

    private String doQueryUrl(MultiValueMap<String, Object> in) {
        return in.entrySet()
                .stream()
                .map(x -> x.getKey() + "=" + x.getValue().get(0))
                .collect(Collectors.joining("&"));
    }

    private void doFetchOrderInfo(Long startTime) {
        toBetInfoX(getGameRounds(startTime))
                .stream()
                .forEach(x -> Optional.ofNullable(toOrder(x))
                        .ifPresent(y -> kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), y)));
    }

    private String getGameRounds(Long unixTime) {
        String url = this.gameUrl + PPConstants.GAME_ROUND;
        UriComponents uriComponentsBase = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("login", this.secretKey)
                .queryParam("password", this.others)
                .queryParam("timepoint", unixTime)
                .queryParam("dataType", "RNG")
                .build();
        return restTemplate.getForObject(uriComponentsBase.toUriString(), String.class);
    }

    private BetOrderVO toOrder(PPGameBetInfoVO betInfo) {
        PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, betInfo.getExtPlayerID());
        if (Objects.isNull(platformGameMember)) return null;
        BetOrderVO betOrder = new BetOrderVO();
        betOrder.setTransactionNo(betInfo.getParentSessionID());
        betOrder.setTransactionSerial(String.valueOf(betInfo.getPlaySessionID()));
        LocalDateTime startDate = DateUtil.toLocalDateTime(betInfo.getStartDate(), "yyyy-MM-dd hh:mm:ss");
        betOrder.setTransactionTime(startDate);
        betOrder.setMemberId(platformGameMember.getMemberId());
        betOrder.setUsername(platformGameMember.getUsername().substring(3));
        betOrder.setPlatformId(platformGameMember.getPlatformId());
        betOrder.setGameId(betInfo.getGameID());
        betOrder.setGameName(betInfo.getType());
        betOrder.setGameCategoryCode(betInfo.getType());
        betOrder.setValidBetAmount(BigDecimal.ZERO);
        betOrder.setBetAmount(betInfo.getBet());

        if (betInfo.getStatus().equals(PPBetInfoStatusEnum.BET_STATUS_SETTLED.getStatus())) {
            betOrder.setSettle(betInfo.getWin());
            LocalDateTime endDate = DateUtil.toLocalDateTime(betInfo.getEndDate(), "yyyy-MM-dd hh:mm:ss");
            betOrder.setSettleTime(endDate);
            betOrder.setBetState(PPBetInfoStatusEnum.BET_STATUS_SETTLED.getValue());
        } else {
            betOrder.setBetState(PPBetInfoStatusEnum.BET_STATUS_PROCESS.getValue());
        }

        betOrder.setWinLoss(betInfo.getWin());
        betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
        betOrder.setPlatformCode(PLATFORM_CODE);

        return betOrder;
    }

    private List<PPGameBetInfoVO> toBetInfoX(String ppGameRoundDataSet) {
        String[] row = ppGameRoundDataSet.split("\n");
        log.info("PP betInfo rowSize {}", row.length);
        List<String> rows = Arrays.asList(row);
        return IntStream.range(2, rows.size())
                .mapToObj(y -> toPPGameBetInfoVO(rows.get(y).split(",")))
                .collect(Collectors.toList());
    }

    private PPGameBetInfoVO toPPGameBetInfoVO(String[] columnArr) {
        return PPGameBetInfoVO.builder()
                .playerID(columnArr[0])
                .extPlayerID(columnArr[1])
                .gameID(columnArr[2])
                .playSessionID(columnArr[3])
                .parentSessionID(columnArr[4])
                .startDate(columnArr[5])
                .endDate(columnArr[6])
                .status(columnArr[7])
                .type(columnArr[8])
                .bet(new BigDecimal(columnArr[9]))
                .win(new BigDecimal(columnArr[10]))
                .currency(columnArr[11])
                .jackpot(new BigDecimal(columnArr[12]))
                .build();
    }

    private Long toUnixMilliTime(LocalDateTime time) {
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

}
