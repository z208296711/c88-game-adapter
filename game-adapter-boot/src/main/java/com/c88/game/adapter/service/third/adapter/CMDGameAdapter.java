package com.c88.game.adapter.service.third.adapter;

import cn.hutool.core.exceptions.ExceptionUtil;
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
import com.c88.game.adapter.service.ICmdVersionsService;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.IRetryFetchOrder;
import com.c88.game.adapter.service.third.vo.CmdBetOrderVO;
import com.c88.game.adapter.service.third.vo.MPGameBetVO;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.utils.DateUtil;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class CMDGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final RestTemplate restTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    private final IPlatformService iplatformService;

    private final ICmdVersionsService iCmdVersionsService;

    private final IRetryFetchOrder retryFetchOrderImpl;

    private static final String PLATFORM_CODE = "CMD";

    public static final Integer SUCCESS_CODE = 0;

    private String partnerKey;

    private String gameUrl;

    private String apiUrl;

    private String prefix;

    private String suffix;

    private String templateName;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .oneOpt()
                .orElse(Platform.builder().code(PLATFORM_CODE).enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());

        ApiParameter apiParameter = platform.getApiParameter();
        this.partnerKey = apiParameter.getApiId();
        this.gameUrl = apiParameter.getGameUrl();
        this.apiUrl = apiParameter.getApiUrl();
        this.templateName = "aliceblue";
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

    @Override
    public Result<String> login(String username, Map<String, String> param) {
        long token = System.currentTimeMillis();
        redisTemplate.opsForValue().set(RedisKey.TOKEN_VALID_BY_CMD + ":" + token, username, 10, TimeUnit.SECONDS);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.gameUrl);
        uriComponentsBuilder.queryParam("user", this.getUsername(username));
        uriComponentsBuilder.queryParam("currency", "PHP");
        uriComponentsBuilder.queryParam("lang", "en-US");
        uriComponentsBuilder.queryParam("templatename", this.templateName);
        uriComponentsBuilder.queryParam("view", param.get("GameId"));
        uriComponentsBuilder.queryParam("token", token);
        log.info(uriComponentsBuilder.toUriString());
        return Result.success(uriComponentsBuilder.toUriString());
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(this.apiUrl)
                .queryParam("Method", "createmember")
                .queryParam("UserName", this.getUsername(username))
                .queryParam("PartnerKey", this.partnerKey)
                .queryParam("Currency", "PHP")
                .build();

        log.info("cmd register url: {}", uriComponents.toUriString());

        String cmdResponse = restTemplate.getForObject(uriComponents.toUriString(), String.class);
        JSONObject jsonResult = JSON.parseObject(cmdResponse);
        Integer code = jsonResult.getInteger("Code");

        if (!SUCCESS_CODE.equals(code) && -98 != code) {
            return Result.failed(jsonResult.getString("Message"));
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

    @Override
    public Result<BigDecimal> balance(String username) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl);
        uriComponentsBuilder.queryParam("method", "getbalance");
        uriComponentsBuilder.queryParam("username", this.getUsername(username));
        uriComponentsBuilder.queryParam("partnerKey", this.partnerKey);
        log.info("cmd findMemberBalance url: {}", uriComponentsBuilder.toUriString());

        try {
            String cmdResponse = restTemplate.getForObject(uriComponentsBuilder.toUriString(), String.class);
            JSONObject jsonResult = JSON.parseObject(cmdResponse);
            Integer code = jsonResult.getInteger("Code");
            if (SUCCESS_CODE.equals(code)) {
                JSONArray data = jsonResult.getJSONArray("Data");
                JSONObject paymentJson = data.getJSONObject(0);
                BigDecimal betAmount = paymentJson.getBigDecimal("BetAmount");
                return Result.success(betAmount);
            }
            return Result.failed(jsonResult.getString("Message"));
        } catch (Exception ex) {
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl);
            uriComponentsBuilder.queryParam("method", "balancetransfer");
            uriComponentsBuilder.queryParam("username", this.getUsername(username));
            uriComponentsBuilder.queryParam("partnerKey", this.partnerKey);
            uriComponentsBuilder.queryParam("paymentType", 1);
            uriComponentsBuilder.queryParam("money", amount);
            uriComponentsBuilder.queryParam("ticketNo", transactionNo);
            log.info("CMD transferIn url: {}", uriComponentsBuilder.toUriString());
            String cmdResponse = restTemplate.getForObject(uriComponentsBuilder.toUriString(), String.class);
            log.info("CMD transferIn response: {}", cmdResponse);
            JSONObject jsonResult = JSON.parseObject(cmdResponse);
            Integer code = jsonResult.getInteger("Code");

            if (SUCCESS_CODE.equals(code)) {
                JSONObject data = jsonResult.getJSONObject("Data");
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.SUCCESS)
                                .build()
                );
            }

        } catch (Exception e) {
            log.error("CMD transferIn Exception : {}", ExceptionUtil.stacktraceToString(e));
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
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl);
            uriComponentsBuilder.queryParam("method", "balancetransfer");
            uriComponentsBuilder.queryParam("username", this.getUsername(username));
            uriComponentsBuilder.queryParam("partnerKey", this.partnerKey);
            uriComponentsBuilder.queryParam("paymentType", 0);
            uriComponentsBuilder.queryParam("money", amount);
            uriComponentsBuilder.queryParam("ticketNo", transactionNo);
            log.info("cmd transfer url: {}", uriComponentsBuilder.toUriString());

            String cmdResponse = restTemplate.getForObject(uriComponentsBuilder.toUriString(), String.class);
            JSONObject jsonResult = JSON.parseObject(cmdResponse);
            Integer code = jsonResult.getInteger("Code");
            if (SUCCESS_CODE.equals(code)) {
                JSONObject data = jsonResult.getJSONObject("Data");
                return Result.success(
                        TransferStateVO.builder()
                                .balance(amount)
                                .state(AdapterTransferStateEnum.SUCCESS)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("CMD transferOut Exception : {}", ExceptionUtil.stacktraceToString(e));
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

    //todo 轉帳單號需新增
    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl);
            uriComponentsBuilder.queryParam("method", "checkfundtransferstatus");
            uriComponentsBuilder.queryParam("username", this.getUsername(username));
            uriComponentsBuilder.queryParam("partnerKey", this.partnerKey);
            uriComponentsBuilder.queryParam("ticketNo", orderId);

            log.info("cmd findTicketStatus url: {}", uriComponentsBuilder.toUriString());
            String cmdResponse = restTemplate.getForObject(uriComponentsBuilder.toUriString(), String.class);
            JSONObject jsonResult = JSON.parseObject(cmdResponse);
            String msg = jsonResult.getString("Message");
            if ("Success".equals(msg)) {
                JSONObject data = jsonResult.getJSONObject("Data");
                Integer status = data.getInteger("orderStatus");
                if (status == 0) {
                    return Result.success("TRANSFER_PROCESSING");
                }
                if (status == 1) {
                    return Result.success("TRANSFER_SUCCESS");
                }
                if (status == 2) {
                    return Result.success("TRANSFER_FAIL");
                }

                return Result.success("TRANSFER_PROCESSING");
            }
            return Result.failed(msg);
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
        List<CmdBetOrderVO> dataList = this.fetchBetOrderVersion(Long.valueOf(param.getOrDefault("version", "0")));
        this.saveOrder(dataList);
    }
    /**
     * 拉取注單
     */
    @Override
    @Transactional
    public void doFetchBetOrderAction() {
        CmdVersions lastVersion = iCmdVersionsService.getLastVersion(this.getGamePlatformCode());
        if (lastVersion == null) {
            lastVersion = new CmdVersions();
            lastVersion.setPlatformCode(this.getGamePlatformCode());
            lastVersion.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue());
            lastVersion.setVersion(0L);
        }
        Long newVersion = lastVersion.getVersion();
        List<CmdBetOrderVO> dataList ;
        try {
            dataList = this.fetchBetOrderVersion(newVersion);
            if (CollectionUtils.isNotEmpty(dataList)) {
                lastVersion.setVersion(this.saveOrder(dataList));
                iCmdVersionsService.saveOrUpdate(lastVersion);
            }
        } catch (Exception ex) {
            XxlJobHelper.log("CMD_exception:{}",ex.toString());
            CmdVersions errorBetOrder = new CmdVersions();
            errorBetOrder.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_ERROR.getValue());
            errorBetOrder.setVersion(newVersion);
            errorBetOrder.setPlatformCode(this.getGamePlatformCode());
            iCmdVersionsService.saveOrUpdate(errorBetOrder);
            Object retryFetchOrder = retryFetchOrderImpl.retryFetchOrderVersion(errorBetOrder);
            if (Objects.nonNull(retryFetchOrder)){
                errorBetOrder.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_DONE.getValue());
                iCmdVersionsService.saveOrUpdate(errorBetOrder);
                if(CollectionUtils.isNotEmpty((List<CmdBetOrderVO>)retryFetchOrder)) {
                    lastVersion.setVersion(this.saveOrder((List<CmdBetOrderVO>) retryFetchOrder));
                    iCmdVersionsService.saveOrUpdate(lastVersion);
                }
            }
        }
    }

    @Override
    public List<CmdBetOrderVO> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    @Override
    public List<CmdBetOrderVO> fetchBetOrderVersion(Long version) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.apiUrl);
        uriComponentsBuilder.queryParam("method", "betrecord");
        uriComponentsBuilder.queryParam("partnerKey", this.partnerKey);
        uriComponentsBuilder.queryParam("Version", version);

        log.info("request:{}", uriComponentsBuilder.toUriString());
        String cmdResponse = restTemplate.getForObject(uriComponentsBuilder.toUriString(), String.class);
        JSONObject jsonResult = JSON.parseObject(cmdResponse);
        Integer code = jsonResult.getInteger("Code");
        if (!SUCCESS_CODE.equals(code)) {
            throw new RuntimeException(jsonResult.getString("Message"));
        }
        return jsonResult.getJSONArray("Data").toJavaList(CmdBetOrderVO.class);
    }


    private String getOriginUsername(String username) {
        return username.substring(3, username.length() - 3);
    }

    @Transactional
    public Long saveOrder(List<CmdBetOrderVO> list) {

        //電子遊戲, 除了ES其他都是體育
        Set<String> sportSet = Set.of("ES");

        //输赢状态
        //WA = Win All
        //WH = Win Half
        //LA = Lose All
        //LH = Lose Half
        //D = Draw
        //P = Pending
        //P 为未结算其他为已经结算
        Set<String> winLoseStatusSet = Set.of("WA", "WH", "LA", "LH", "D");

        //D: 正在处理的滚球赛事的单
        //N: 已接受今日或早盘的单
        //A: 已接受的滚球赛事的单
        //C: 已取消的单（一般为球赛取消造成）
        //R: 已拒绝的单
        Set<String> dangerStatusSet = Set.of("C", "R");
        AtomicReference<Long> version = new AtomicReference<>(0L);
        list.forEach(vo -> {
            Integer settleNote = null;
            PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, vo.getSourceName());
            if (platformGameMember == null) {
                return;
            }

            String gameType = "sport";
            String gameId = "v1";
            if (sportSet.contains(vo.getSportType())) {
                gameType = "esport";
                gameId = "v3";
            }

            BigDecimal winLoss = BigDecimal.ZERO;
            BigDecimal validBet = BigDecimal.ZERO;
            Integer status = BetOrderTypeEnum.BET_STATUS_CONFIRMED.getValue();
            if (winLoseStatusSet.contains(vo.getWinLoseStatus())) {
                status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
                winLoss = vo.getWinAmount();
                validBet = this.getValidBet(winLoss, vo.getBetAmount());
            }

            if (vo.getDangerStatus().equals("D")) {
                return;
            }
            if (dangerStatusSet.contains(vo.getDangerStatus())) {
                settleNote = ReBateStatusEnum.CANCEL.getValue();
                status = BetOrderTypeEnum.BET_STATUS_CANCELED.getValue();
            }

            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo(vo.getReferenceNo());
            betOrder.setTransactionSerial(String.valueOf(vo.getId()));
            betOrder.setTransactionTime(DateUtil.convertFromTicks(vo.getTransDate()));

            betOrder.setMemberId(platformGameMember.getMemberId());
            betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
            betOrder.setPlatformId(platformGameMember.getPlatformId());

            betOrder.setGameId(gameId);
            betOrder.setGameName(gameType); // 只有電競&體育兩種

            betOrder.setGameCategoryCode(gameType);
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(vo.getBetAmount());

            if (status.equals(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betOrder.setSettle(vo.getWinAmount());
                betOrder.setSettleTime(DateUtil.convertFromTicks(vo.getStateUpdateTs()));
            }

            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            betOrder.setPlatformCode(PLATFORM_CODE);
            betOrder.setSettleNote(settleNote);
            //發送Mq
            kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);

            version.set(vo.getId());
        });
        return version.get();
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
