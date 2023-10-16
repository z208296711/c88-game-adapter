package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.DateUtil;
import com.c88.common.core.util.GameUtil;
import com.c88.common.core.util.HttpUtil;
import com.c88.game.adapter.constants.JILIConstants;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.mapper.GameCategoryMapper;
import com.c88.game.adapter.pojo.entity.*;
import com.c88.game.adapter.service.*;
import com.c88.game.adapter.service.third.vo.JiLiBetOrderVO;
import com.c88.game.adapter.service.third.vo.JiLiPageVO;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.c88.game.adapter.constants.JILIConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JILIGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private final IFetchRecordService iFetchRecordService;

    private final IPlatformGameService gameService;

    private final GameCategoryMapper gameCategoryMapper;
    private final IRetryFetchOrder retryFetchOrderImpl;

    private String agentId;

    private String agentKey;

    private String apiUrl;

    private String prefix;

    private String suffix;

    public static final String formatString = "yyyy-MM-dd'T'HH:mm:ss";

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.agentId = apiParameter.getApiId();
        this.agentKey = apiParameter.getApiKey();
        this.apiUrl = apiParameter.getApiUrl();
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
        final String url = apiUrl + LOGIN_URL;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Account", this.getUsername(username));
        map.put("GameId", param.get("GameId"));
        map.put("Lang", JILIConstants.LANG_DEFAULT);
        generateKeyAndAgentId(map);
        String queryString = HttpUtil.parseMapToString(map);
        log.info("JIL login url: {}, queryString : {}", url, queryString);

        ResponseEntity<JSONObject> jsonResult = restTemplate.getForEntity(URI.create(url + "?" + queryString), JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (HttpStatus.OK.equals(httpStatus) && jsonBody.getInteger("ErrorCode") == 0) {
            return Result.success(jsonBody.getString("Data"));
        }
        return Result.failed(jsonBody.getString("Message"));
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        final String url = apiUrl + REGISTER_URL;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Account", this.getUsername(username));
        generateKeyAndAgentId(map);
        log.info("JIL login url: {}, request : {}", url, map);

        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, map, JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (HttpStatus.OK.equals(httpStatus) && jsonBody.getInteger("ErrorCode") == 0) {
            iPlatformGameMemberService.save(
                    PlatformGameMember.builder()
                            .memberId(memberId)
                            .username(this.getUsername(username))
                            .platformId(platform.getId())
                            .code(PLATFORM_CODE)
                            .build());
            return Result.success(jsonBody.getString("Data"));
        }
        return Result.failed(jsonBody.getString("Message"));
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        final String url = apiUrl + BALANCE_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("Accounts", this.getUsername(username));
        generateKeyAndAgentId(map);

        try {
            ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

            HttpStatus httpStatus = jsonResult.getStatusCode();
            JSONObject jsonBody = jsonResult.getBody();

            if (HttpStatus.OK.equals(httpStatus) && jsonBody.getInteger("ErrorCode") == 0) {
                BigDecimal balance = jsonBody.getJSONArray("Data").getJSONObject(0).getBigDecimal("Balance");
                return Result.success(balance);
            }
            return Result.failed(jsonBody.getString("Message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, TransferTypeEnum.IN);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        return transfer(username, amount, transactionNo, TransferTypeEnum.OUT);
    }

    private Result<TransferStateVO> transfer(String username, BigDecimal amount, String transactionNo, TransferTypeEnum transferType) {
        final String url = apiUrl + TURN_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("Account", this.getUsername(username));
        map.add("TransactionId", transactionNo);
        map.add("Amount", amount);
        map.add("TransferType", transferType.typeCode);
        generateKeyAndAgentId(map);

        try {
            ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, new HttpEntity<>(map, headers), JSONObject.class);

            HttpStatus httpStatus = jsonResult.getStatusCode();
            JSONObject jsonBody = jsonResult.getBody();

            if (!HttpStatus.OK.equals(httpStatus) || jsonBody.getInteger("ErrorCode") != 0) {
                log.error("JILI transfer fail:{}", jsonBody.getString("Message"));
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.FAIL)
                                .build()
                );
            }

            return Result.success(
                    TransferStateVO.builder()
                            .balance(amount)
                            .state(AdapterTransferStateEnum.SUCCESS)
                            .build()
            );
        } catch (Exception e) {
            log.error("JILI transfer Exception : {}", ExceptionUtil.stacktraceToString(e));
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
        final String url = apiUrl + TURN_STATUS_URL;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("TransactionId", orderId);
        generateKeyAndAgentId(map);
        ResponseEntity<JSONObject> jsonResult = restTemplate.postForEntity(url, map, JSONObject.class);

        HttpStatus httpStatus = jsonResult.getStatusCode();
        JSONObject jsonBody = jsonResult.getBody();

        if (!HttpStatus.OK.equals(httpStatus) || jsonBody.getInteger("ErrorCode") != 0) {
            return Result.failed(jsonBody.getString("Message"));
        }
        String result = jsonBody.getInteger("Status") == 1 ? "SUCCESS" : "FAIL";
        return Result.success(result);
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
        List<JiLiBetOrderVO> dataList = this.fetchBetOrder(startDateTime, endDateTime);
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
            List<JiLiBetOrderVO> list = this.fetchBetOrder(startTime, endTime);
            if (CollectionUtils.isNotEmpty(list)) {
                this.saveOrder(list);
            }
            iFetchRecordService.saveOrUpdate(fetchRecord);
        } catch (Exception ex) {
            XxlJobHelper.log("JILI_exception:{}",ex.toString());
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
                if(CollectionUtils.isNotEmpty((List<JiLiBetOrderVO>)retryFetchOrder)) {
                    this.saveOrder((List<JiLiBetOrderVO>)retryFetchOrder);
                }
            }
        }
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    public List<JiLiBetOrderVO> fetchBetOrder(LocalDateTime start, LocalDateTime end) {
        final String url = apiUrl + BET_RECORD_URL;
        Map<String, Object> map = new LinkedHashMap<>();
        String startTimeString = DateUtil.dateToStr(toServerTime(start), formatString);
        String endTimeString = DateUtil.dateToStr(toServerTime(end), formatString);
        map.put("StartTime", startTimeString);
        map.put("EndTime", endTimeString);
        map.put("Page", 1);
        map.put("PageLimit", 20000);
        generateKeyAndAgentId(map);
        String queryString = HttpUtil.parseMapToString(map);
        log.info("JIL bet log url: {}, queryString : {}", url, queryString);
        XxlJobHelper.log("JIL bet log url: {}, queryString : {}", url, queryString);
        ResponseEntity<JSONObject> jsonResult = restTemplate.getForEntity(URI.create(url + "?" + queryString), JSONObject.class);

        JSONObject jsonBody = jsonResult.getBody();
        if (!"0".equals(jsonBody.getString("ErrorCode"))) {
            throw new RuntimeException(jsonBody.getString("Message"));
        }
        JSONObject data = jsonBody.getJSONObject("Data");
        JiLiPageVO pagination = data.getObject("Pagination", JiLiPageVO.class);
        List<JiLiBetOrderVO> returnList = data.getJSONArray("Result").toJavaList(JiLiBetOrderVO.class);
        if (pagination.getTotalPages() > 1) {
            for (int i = 1; i < pagination.getTotalPages(); i++) {
                fetchBetOrderByPage(startTimeString, endTimeString, url, returnList, i + 1);
            }
        }
        return returnList;
    }

    private void fetchBetOrderByPage(String startTimeString, String endTimeString, String url, List<JiLiBetOrderVO> returnList, int page) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("StartTime", startTimeString);
        map.put("EndTime", endTimeString);
        map.put("Page", page);
        map.put("PageLimit", 20000);
        generateKeyAndAgentId(map);
        String queryString = HttpUtil.parseMapToString(map);
        log.info("JIL bet log url: {}, page : {}", url, page);
        XxlJobHelper.log("JIL bet log url: {}, page : {}", url, page);
        ResponseEntity<JSONObject> jsonResult = restTemplate.getForEntity(URI.create(url + "?" + queryString), JSONObject.class);

        JSONObject jsonBody = jsonResult.getBody();
        if (!"0".equals(jsonBody.getString("ErrorCode"))) {
            throw new RuntimeException(jsonBody.getString("Message"));
        }
        JSONObject data = jsonBody.getJSONObject("Data");
        returnList.addAll(data.getJSONArray("Result").toJavaList(JiLiBetOrderVO.class));
    }

    @Transactional
    public Long saveOrder(List<JiLiBetOrderVO> list) {
        Map<String, PlatformGame> gameMap = gameService.getPlatformGameByPlatform(PLATFORM_CODE).stream().collect(Collectors.toMap(PlatformGame::getGameId, Function.identity()));
        List<GameCategory> gameCategoryList = gameCategoryMapper.selectList(new LambdaQueryWrapper<>());
        AtomicReference<Long> version = new AtomicReference<>(0L);
        list.forEach(vo -> {
            vo.setBetAmount(vo.getBetAmount().abs());//jili回傳投注額是負的
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, vo.getAccount());
            if (platformGameMember == null) {
                return;
            }
            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            if (vo.getPayoffTime() != null) {
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
            }
            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getWagersId());
            betOrder.setTransactionSerial(vo.getWagersId());
            betOrder.setTransactionTime(vo.getWagersTime());

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());
            betOrder.setGameId(vo.getGameId());
            PlatformGame platformGame = gameMap.get(vo.getGameId());
            betOrder.setGameName(platformGame.getNameEn());
            GameCategory category = gameCategoryList.stream().filter(gameCategory ->
                    gameCategory.getId().equals(platformGame.getGameCategoryId())).findFirst().get();
            betOrder.setGameCategoryCode(category.getCode());

            betOrder.setValidBetAmount(vo.getBetAmount());
            betOrder.setBetAmount(vo.getBetAmount());
            if (vo.getPayoffTime() != null) {
                betOrder.setSettle(vo.getPayoffAmount());
                betOrder.setSettleTime(vo.getPayoffTime());
            }
            betOrder.setWinLoss(vo.getPayoffAmount().subtract(vo.getBetAmount()));
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(PLATFORM_CODE);
            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

        });
        return version.get();
    }

    private void generateKeyAndAgentId(Map<String, Object> map) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMd");
        String dateString = LocalDateTime.now(ZoneOffset.of("-4")).format(formatter);
        map.put("AgentId", agentId);
        StringJoiner sb = new StringJoiner("&");
        for (Map.Entry<String, Object> param : map.entrySet()) {
            sb.add(param.getKey() + "=" + param.getValue());
        }
        String keyG = GameUtil.getMD5(dateString + agentId + agentKey);
        String md5String = GameUtil.getMD5(sb + keyG);
        String randomString = RandomStringUtils.randomAlphanumeric(6);
        String randomString2 = RandomStringUtils.randomAlphanumeric(6);
        String key = randomString + md5String + randomString2;
        map.put("Key", key);
    }

    private void generateKeyAndAgentId(MultiValueMap<String, Object> map) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMd");
        String dateString = LocalDateTime.now(ZoneOffset.of("-4")).format(formatter);
        map.add("AgentId", agentId);
        StringJoiner sb = new StringJoiner("&");
        for (Map.Entry<String, List<Object>> param : map.entrySet()) {
            sb.add(param.getKey() + "=" + param.getValue().get(0));
        }
        String keyG = GameUtil.getMD5(dateString + agentId + agentKey);
        String md5String = GameUtil.getMD5(sb + keyG);
        String randomString = RandomStringUtils.randomAlphanumeric(6);
        String randomString2 = RandomStringUtils.randomAlphanumeric(6);
        String key = randomString + md5String + randomString2;
        map.add("Key", key);
    }

    private enum TransferTypeEnum {
        IN((byte) 2),
        OUT((byte) 3);
        byte typeCode;

        TransferTypeEnum(byte typeCode) {
            this.typeCode = typeCode;
        }
    }

    private static LocalDateTime toServerTime(LocalDateTime localDateTime) {
        ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.ofHours(-4));
        return converted.toLocalDateTime();
    }

}
