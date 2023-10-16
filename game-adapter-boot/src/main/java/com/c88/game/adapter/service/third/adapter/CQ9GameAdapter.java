package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.GameUtil;
import com.c88.game.adapter.constants.CQ9Constants;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Cq9BetOrderTime;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.service.ICq9BetOrderTimeService;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.utils.DateUtil;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.c88.game.adapter.constants.AELotteryConstants.PLATFORM_CODE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CQ9GameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private final ICq9BetOrderTimeService iCq9BetOrderTimeService;

    private String prefix;
    private String apiUrl;
    private String header;
    private String vendorId;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, CQ9Constants.PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.header = apiParameter.getApiKey();
        this.vendorId = apiParameter.getApiId();
        this.apiUrl = apiParameter.getApiUrl();
        this.prefix = apiParameter.getPrefix();
    }

    @Override
    public String getUsername(String username) {
        return prefix + username;
    }

    @Override
    public String getGamePlatformCode() {
        return CQ9Constants.PLATFORM_CODE;
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {
        String url = apiUrl + CQ9Constants.GAME_URL;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("usertoken", this.getUserToken(username));
        jsonObject.put("gamehall", CQ9Constants.PLATFORM_CODE);
        jsonObject.put("gamecode", param.get("GameId"));
        jsonObject.put("gameplat", param.get("GameId"));
        jsonObject.put("lang", CQ9Constants.LANG_DEFAULT);

        log.info("CQ9 login url: {}, jsonObject : {}", url, JSON.toJSONString(jsonObject));

        String response = restTemplate.postForObject(url, jsonObject, String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        JSONObject status = jsonResult.getJSONObject("status");
        String code = status.getString("code");

        if (!CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
            return Result.failed(status.getString("message"));
        }
        return Result.success(jsonResult.getJSONObject("data").getString("url"));
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        String url = apiUrl + CQ9Constants.REGISTER_URL;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));
        jsonObject.put("password", GameUtil.getMD5(username));
        jsonObject.put("nickname", this.getUsername(username));

        log.info("CQ9 register url: {}, jsonObject : {}", url, JSON.toJSONString(jsonObject));

        String response = restTemplate.postForObject(url, jsonObject, String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        JSONObject status = jsonResult.getJSONObject("status");
        String code = status.getString("code");

        if (!CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
            return Result.failed(status.getString("message"));
        }

        iPlatformGameMemberService.save(
                PlatformGameMember.builder()
                        .memberId(memberId)
                        .username(this.getUsername(username))
                        .platformId(platform.getId())
                        .code(CQ9Constants.PLATFORM_CODE)
                        .build()
        );
        return Result.success(username);
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        String url = apiUrl + CQ9Constants.BALANCE_URL + this.getUsername(username);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(url);

        log.info("CQ9 findMemberBalance url: {}", uriComponentsBuilder.toUriString());

        try {
            String response = restTemplate.getForObject(uriComponentsBuilder.toUriString(), String.class);
            JSONObject jsonResult = JSON.parseObject(response);
            JSONObject status = jsonResult.getJSONObject("status");
            String code = status.getString("code");

            if (CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
                JSONObject data = jsonResult.getJSONObject("data");
                BigDecimal betAmount = data.getBigDecimal("balance");
                return Result.success(betAmount);
            }
            return Result.failed(status.getString("message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        try {
            String url = apiUrl + CQ9Constants.TURN_IN_URL;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("account", this.getUsername(username));
            jsonObject.put("mtcode", transactionNo);
            jsonObject.put("amount", amount);

            log.info("CQ9 transfer in url: {}, jsonObject : {}", url, JSON.toJSONString(jsonObject));

            String response = restTemplate.postForObject(url, jsonObject, String.class);
            JSONObject jsonResult = JSON.parseObject(response);
            JSONObject status = jsonResult.getJSONObject("status");
            String code = status.getString("code");

            if (CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
                JSONObject data = jsonResult.getJSONObject("data");
                BigDecimal betAmount = data.getBigDecimal("balance");
//                return Result.success(betAmount);

                TransferStateVO stateVO = new TransferStateVO();
                stateVO.setBalance(betAmount);
                stateVO.setState(AdapterTransferStateEnum.SUCCESS);
                return Result.success(stateVO);
            }
            return Result.failed(status.getString("message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        try {
            String url = apiUrl + CQ9Constants.TURN_OUT_URL;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("account", this.getUsername(username));
            jsonObject.put("mtcode", transactionNo);
            jsonObject.put("amount", amount);

            log.info("CQ9 transfer out url: {}, jsonObject : {}", url, JSON.toJSONString(jsonObject));

            String response = restTemplate.postForObject(url, jsonObject, String.class);
            JSONObject jsonResult = JSON.parseObject(response);
            JSONObject status = jsonResult.getJSONObject("status");
            String code = status.getString("code");

            if (CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
                JSONObject data = jsonResult.getJSONObject("data");
                BigDecimal betAmount = data.getBigDecimal("balance");
                TransferStateVO stateVO = new TransferStateVO();
                stateVO.setBalance(betAmount);
                stateVO.setState(AdapterTransferStateEnum.SUCCESS);
                return Result.success(stateVO);
            }
            return Result.failed(status.getString("message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        try {
            String url = apiUrl + CQ9Constants.TURN_STATUS_URL + orderId;
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url);

            log.info("CQ9 findTicketStatus url: {}", uriComponentsBuilder.toUriString());

            String response = restTemplate.getForObject(url, String.class);
            JSONObject jsonResult = JSON.parseObject(response);
            JSONObject status = jsonResult.getJSONObject("status");
            String code = status.getString("code");

            if (CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
                JSONObject data = jsonResult.getJSONObject("data");
                String txStatus = data.getString("status");
                if (txStatus.equals("success")) {
                    return Result.success("TRANSFER_SUCCESS");
                }
                if (txStatus.equals("failed")) {
                    return Result.success("TRANSFER_FAIL");
                }
                if (txStatus.equals("pending")) {
                    return Result.success("TRANSFER_PROCESSING");
                }
            }
            return Result.failed(status.getString("message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        Long startTime = startDateTime.toInstant(ZoneOffset.ofHours(-4)).toEpochMilli();
        Long endTime = endDateTime.toInstant(ZoneOffset.ofHours(-4)).toEpochMilli();
        String data = this.fetchBetOrder(startTime, endTime, Integer.parseInt(param.get("page")));
        JSONArray response = JSONArray.parseArray(data);
        this.saveOrder(response);
    }

    @Override
    public void doFetchBetOrderAction() {
        Long startTime;
        Long endTime;
        Cq9BetOrderTime cq9BetOrderTime = iCq9BetOrderTimeService.getLastVersion();
        if (cq9BetOrderTime == null) {
            cq9BetOrderTime = new Cq9BetOrderTime();
            cq9BetOrderTime.setStartTime(System.currentTimeMillis());
            cq9BetOrderTime.setEndTime(System.currentTimeMillis());
        }

        startTime = cq9BetOrderTime.getStartTime();
        endTime = cq9BetOrderTime.getEndTime();
        if (cq9BetOrderTime.getEndTime().equals(0L)) {
            cq9BetOrderTime.setEndTime(System.currentTimeMillis());
            endTime = cq9BetOrderTime.getEndTime();
        }
        Integer page = 1;
        boolean goNextPage = false;
        do {
            try {
                String data = this.fetchBetOrder(startTime, endTime, page);
                JSONArray response = JSONArray.parseArray(data);

                if (CollectionUtils.isNotEmpty(response)) {
                    this.saveOrder(response);
                    cq9BetOrderTime.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_DONE.getValue());
                    iCq9BetOrderTimeService.saveOrUpdate(cq9BetOrderTime);
                    if (response.size() == 20000) {
                        goNextPage = true;
                        page++;
                    }
                }
            } catch (Exception ex) {
                cq9BetOrderTime.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_ERROR.getValue());

                Cq9BetOrderTime nextBetOrderTime = new Cq9BetOrderTime();
                nextBetOrderTime.setStartTime(startTime);
                nextBetOrderTime.setEndTime(endTime);
                nextBetOrderTime.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue());
                iCq9BetOrderTimeService.saveBatch(Arrays.asList(cq9BetOrderTime, nextBetOrderTime));
            }
        } while (goNextPage);

        Cq9BetOrderTime nextBetOrderTime = new Cq9BetOrderTime();
        nextBetOrderTime.setStartTime(endTime);
        nextBetOrderTime.setEndTime(0L);
        nextBetOrderTime.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue());
        iCq9BetOrderTimeService.save(nextBetOrderTime);
    }

    @Override
    public <T> List<T> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    private Result<String> getUserToken(String username) {
        String url = apiUrl + CQ9Constants.TOKEN_URL;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", this.getUsername(username));
        jsonObject.put("password", GameUtil.getMD5(username));

        log.info("CQ9 token url: {}, jsonObject : {}", url, JSON.toJSONString(jsonObject));

        String response = restTemplate.postForObject(url, jsonObject, String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        JSONObject status = jsonResult.getJSONObject("status");
        String code = status.getString("code");

        if (!CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
            return Result.failed(status.getString("message"));
        }

        return Result.success(jsonResult.getJSONObject("data").getString("usertoken"));
    }

    private String fetchBetOrder(Long startTime, Long endTime, Integer page) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss-04:00";
        String url = apiUrl + CQ9Constants.BET_RECORD_URL;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url);
        uriComponentsBuilder.queryParam("starttime", DateUtil.toDateString(startTime, pattern));
        uriComponentsBuilder.queryParam("endtime", DateUtil.toDateString(endTime, pattern));
        uriComponentsBuilder.queryParam("page", page);
        uriComponentsBuilder.queryParam("pagesize", 20000);

        log.info("CQ9 fetchBetOrder url: {}", uriComponentsBuilder.toUriString());

        String response = restTemplate.getForObject(url, String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        JSONObject data = jsonResult.getJSONObject("data");
        JSONObject status = jsonResult.getJSONObject("status");
        String code = status.getString("code");
        if (!CQ9Constants.RESPONSE_SUCCESS.equals(code)) {
            throw new RuntimeException(status.getString("message"));
        }
        return data.getJSONArray("Data").toJSONString();
    }

    @Transactional
    public void saveOrder(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(
                    CQ9Constants.PLATFORM_CODE, jsonObject.getString("account"));
            if (platformGameMember == null) {
                return;
            }

            BigDecimal winLoss = BigDecimal.ZERO;
            BigDecimal validBet = BigDecimal.ZERO;
            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            if (CQ9Constants.BET_SETTLED.equals(jsonObject.getString("status"))) {
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
                winLoss = jsonObject.getBigDecimal("win");
                validBet = jsonObject.getBigDecimal("validbet");
            }

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(jsonObject.getString("round"));
            betOrder.setTransactionSerial(jsonObject.getString("round"));
            betOrder.setTransactionTime(DateUtil.convertFromTicks(DateUtil.toTimestamp(
                    jsonObject.getString("createtime"), "yyyy-mm-dd'T'hh:mm:ss-04:00", 0)));

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());
            betOrder.setGameId(jsonObject.getString("gamecode"));
            betOrder.setGameName(jsonObject.getString("gamecode"));

            // TODO: 不確定有沒有其他type
            betOrder.setGameCategoryCode("slot");
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(jsonObject.getBigDecimal("bet"));

            if (status.equals(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betOrder.setSettle(winLoss);
                betOrder.setTransactionTime(DateUtil.convertFromTicks(DateUtil.toTimestamp(
                        jsonObject.getString("createtime"), "yyyy-mm-dd'T'hh:mm:ss-04:00", 0)));
            }

            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(CQ9Constants.PLATFORM_CODE);

            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);
        }
    }

    private String getOriginUsername(String username) {
        return username.substring(3);
    }
}
