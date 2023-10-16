package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.HttpUtil;
import com.c88.game.adapter.constants.DS88Constants;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.*;
import com.c88.game.adapter.repository.IBetOrderRepository;
import com.c88.game.adapter.service.*;
import com.c88.game.adapter.service.third.vo.DS88GameVO;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.SABA.BetDetailsItem;
import com.c88.game.adapter.service.third.vo.SABA.BetNumberDetailsItem;
import com.c88.game.adapter.service.third.vo.SABA.BetVirtualSportDetailsItem;
import com.c88.game.adapter.service.third.vo.SABA.SABABetOrderVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.fasterxml.jackson.annotation.JsonAlias;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Slf4j
@Component
@RequiredArgsConstructor
public class DS88GameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IBetOrderRepository iBetOrderRepository;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    private final IPlatformService iplatformService;

    private final ISabaVersionsService iSabaVersionsService;

    private final IBetOrderService iBetOrderService;

    private final IFetchRecordService iFetchRecordService;

    private final IPlatformGameService iPlatformGameService;
    private final IRetryFetchOrder retryFetchOrderImpl;

    private static final String PLATFORM_CODE = "DS88";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


    private String apiUrl;

    private String vendorId;

    private String operatorId;

    private String prefix;

    private String apiKey;

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
        this.apiKey = apiParameter.getApiKey();
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
        redisTemplate.opsForValue().set(RedisKey.TOKEN_VALID_BY_DS88 + ":" + token, username, 10, TimeUnit.SECONDS);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Basic " + apiKey);

        JSONObject params = new JSONObject();
        params.put("login", this.getUsername(username));
        params.put("password", operatorId);
        params.put("lang", "EN");

        HttpEntity<JSONObject> request = new HttpEntity<>(params, headers);

        String url = apiUrl + DS88Constants.LOGIN_URL;
        JSONObject response = restTemplate.postForObject(url, request, JSONObject.class);

        if (response.getString("code").equalsIgnoreCase(DS88Constants.RESPONSE_SUCCESS)) {
            return Result.success(response.getString("game_link"));
        }
        return Result.failed(response.getString("message"));
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
//        Result<String> result = this.login(username, null);
//        if (!Result.isSuccess(result)) {
//            return Result.failed();
//        }
        String url = apiUrl + DS88Constants.REGISTER_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Basic " + apiKey);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));
        jsonObject.put("password", operatorId);
        jsonObject.put("name", username);
        HttpEntity httpEntity = new HttpEntity<>(jsonObject, headers);

        JSONObject jsonResult = restTemplate.postForObject(url, httpEntity, JSONObject.class);
        log.info("DS88_register response:{}", jsonResult);
        String status = jsonResult.getString("code");

        if (!DS88Constants.RESPONSE_SUCCESS.equalsIgnoreCase(status)) {
            log.info("DS88_register_fail:{}", jsonResult.getString("message"));
            return Result.failed(jsonResult.getString("message"));
        } else {
            iPlatformGameMemberService.save(
                    PlatformGameMember.builder()
                            .memberId(memberId)
                            .username(this.getUsername(username))
                            .platformId(platform.getId())
                            .code(DS88Constants.PLATFORM_CODE)
                            .build()
            );
            return Result.success(username);
        }
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        String url = apiUrl + DS88Constants.BALANCE_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Basic " + apiKey);
        Map params = new LinkedHashMap<>();
        params.put("account", this.getUsername(username));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
        String queryString = HttpUtil.parseMapToString(params);
        try {
            String response = restTemplate.exchange(URI.create(url + "?" + queryString), HttpMethod.GET, request, String.class).getBody();
            JSONObject jsonResult = JSON.parseObject(response);
            log.info("DS88_balance:{}", jsonResult);
            if (DS88Constants.RESPONSE_SUCCESS.equalsIgnoreCase(jsonResult.getString("code"))) {
                return Result.success(jsonResult.getBigDecimal("balance"));
            }
            return Result.failed(jsonResult.getString("Message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, DS88Constants.TRANSFER_IN);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, DS88Constants.TRANSFER_OUT);
    }

    public Result<TransferStateVO> transfer(String username, BigDecimal amount, String transactionNo, Integer direction) {
        String url = apiUrl + (direction==DS88Constants.TRANSFER_IN ? DS88Constants.TRANSFER_IN_URL : DS88Constants.TRANSFER_OUT_URL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Basic " + apiKey);

        JSONObject jsonObject = new JSONObject();


        jsonObject.put("account", this.getUsername(username));
        jsonObject.put("merchant_order_num", transactionNo);
        jsonObject.put("amount", direction==DS88Constants.TRANSFER_IN  ? amount : amount.negate());

        HttpEntity<JSONObject> request = new HttpEntity<>(jsonObject, headers);
        try {
            String response = restTemplate.postForObject(url, request, String.class);
            JSONObject jsonResult = JSON.parseObject(response);

            log.info("DS88_transfer:{}", jsonResult);
            if (DS88Constants.RESPONSE_SUCCESS.equalsIgnoreCase(jsonResult.getString("code"))) {
                TransferStateVO stateVO = new TransferStateVO();
                stateVO.setBalance(jsonResult.getBigDecimal("balance"));
                stateVO.setState(AdapterTransferStateEnum.SUCCESS);
                return Result.success(stateVO);
            } else {
                TransferStateVO stateVO = new TransferStateVO();
                stateVO.setState(AdapterTransferStateEnum.FAIL);
                return Result.success(stateVO);

            }
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        String url = apiUrl + DS88Constants.CHECK_TRANSFER_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Basic " + apiKey);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("merchant_order_num", orderId);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
        String queryString = HttpUtil.parseMapToString(params);

        try {
            String response = restTemplate.exchange(URI.create(url + "?" + queryString), HttpMethod.GET, request, String.class).getBody();
            JSONObject jsonResult = JSON.parseObject(response);
            log.info("DS88_check_transfer:{}", jsonResult);
            String status = jsonResult.getString("code");
            if (status.equalsIgnoreCase(DS88Constants.RESPONSE_SUCCESS)) {
                return Result.success("TRANSFER_SUCCESS");
            }else{
                return Result.success("TRANSFER_FAIL");
            }
//            return Result.success("TRANSFER_PROCESSING");
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
        List<DS88GameVO> dataList = this.fetchBetOrder(startDateTime,endDateTime);
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
            List<DS88GameVO> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("DS88_exception:{}",ex.toString());
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
                if(CollectionUtils.isNotEmpty((List<DS88GameVO>)retryFetchOrder)) {
                    this.saveOrder((List<DS88GameVO>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    @Override
    public List<DS88GameVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        String url = apiUrl + DS88Constants.BET_DETAIL_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Basic " + apiKey);

        boolean isNext = true;
        int page = 1;
        int totalCount;
        List<DS88GameVO> list = new ArrayList<>();
        while(isNext){
            Map<String, Object> params = new LinkedHashMap<>();
//            log.info("+8time:{},sendstart:{},{}, end:{}", startTime,formatDate(startTime), endTime,formatDate(endTime));
            params.put("time_type", "settled_at");
            params.put("start_time", formatDate(startTime));
            params.put("end_time", formatDate(endTime));
            params.put("page", page);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
            String queryString = HttpUtil.parseMapToString(params);
            log.info("ds88 betorder url:{}, request:{}", url, request);
            String response = restTemplate.exchange(URI.create(url + "?" + queryString), HttpMethod.GET, request, String.class).getBody();
            log.info("DS88_betDetail:{}", response);
            JSONObject jsonResult = JSON.parseObject(response);
            JSONArray data = jsonResult.getJSONArray("data");

            if(data.size()==0) return list;

            data.forEach(d -> {
                JSONObject jsonObject = (JSONObject) d;
                DS88GameVO vo = new DS88GameVO();
                vo.setSlug(jsonObject.getString("slug"));
                vo.setArenaFightNo(jsonObject.getString("arena_fight_no"));
                vo.setRoundId(jsonObject.getLong("round_id"));
                vo.setFlightNo(jsonObject.getString("fight_no"));
                vo.setSide(jsonObject.getString("side"));
                vo.setAccount(jsonObject.getString("account"));
                vo.setStatus(jsonObject.getString("status"));
                vo.setOdd(jsonObject.getBigDecimal("odd"));
                vo.setBetAmount(jsonObject.getBigDecimal("bet_amount"));
                vo.setNetIncome(jsonObject.getBigDecimal("net_income"));
                vo.setBetReturn(jsonObject.getBigDecimal("bet_return"));
                vo.setValidAmount(jsonObject.getBigDecimal("valid_amount"));
                vo.setResult(jsonObject.getString("result"));
                vo.setIsSettled(jsonObject.getBoolean("is_settled"));
                vo.setBetAt(toLocal(jsonObject.getString("bet_at")));
                vo.setSettledAt(toLocal(jsonObject.getString("settled_at")));

                list.add(vo);
            });
            totalCount = jsonResult.getInteger("total_count");

            if (totalCount > page * 500) {
                page++;
                try {
                    Thread.sleep(11000l);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                isNext = false;
            }
        };

        return list;

    }

    private String getOriginUsername(String username) {
        return username.substring(3);
    }

    @Transactional
    public Long saveOrder(List<DS88GameVO> list) {
        log.info("DS88 saveorder list:{}", list);
        list.forEach(vo -> {

            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(this.getGamePlatformCode(), vo.getAccount());
            if (platformGameMember == null) {
                return;
            }

            BigDecimal winLoss = vo.getNetIncome();
            BigDecimal validBet = vo.getValidAmount();
            BigDecimal settle = vo.getBetReturn();
            // 派彩= 輸贏 + 投注
            Integer status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getSlug());
            betOrder.setTransactionSerial(vo.getSlug());
            betOrder.setTransactionTime(vo.getBetAt());

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

            //ds88 has no gameid
            PlatformGame platformGame = iPlatformGameService.getPlatformGameByCode(this.getGamePlatformCode(), "");
            log.info("DS88 platformgame:{}", platformGame);
            if (platformGame == null) {
                log.error("platformGame:{} is null");
                return;
            }
            betOrder.setGameId(platformGame.getGameId());
            betOrder.setGameName(platformGame.getNameEn());
            betOrder.setPlatformCode(this.getGamePlatformCode());
            betOrder.setGameCategoryCode("special");
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(validBet);
            betOrder.setSettle(settle);
            betOrder.setSettleTime(vo.getSettledAt());
            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(this.getGamePlatformCode());

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

        });
        return 0l;

    }

    private String formatDate(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static LocalDateTime toLocal(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d = format.parse(date.replace("Z", " UTC"));
            return d.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        }catch (Exception e){
            return null;
        }
    }
}
