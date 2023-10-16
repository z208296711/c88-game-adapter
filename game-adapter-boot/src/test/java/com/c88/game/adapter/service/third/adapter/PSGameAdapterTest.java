package com.c88.game.adapter.service.third.adapter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.mapper.BetOrderMapper;
import com.c88.game.adapter.mapstruct.BetOrderConverter;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import com.c88.game.adapter.service.IMemberBetAmountRecordDailyService;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.vo.BetOrderVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@SpringBootTest(properties = "spring.profiles.active:local")
class PSGameAdapterTest {

    @Autowired
    private PSGameAdapter psGameAdapter;
    ApiParameter apiParameter;

    @BeforeEach
    void init() {
        psGameAdapter.init();
    }

    @Test
    void register() {

        Platform platform = Platform.builder().id(71l).apiParameter(apiParameter).build();
        psGameAdapter.register(9000008l, "terryzhang3", platform);
    }

    @Test
    void login() {
        Map map = new HashMap();
        map.put("GameId", "0");
        Result<String> urlRes = psGameAdapter.login("terryzhang", map);
        log.info(urlRes.getData());
    }


    @Test
    void balance() {
        Result<BigDecimal> urlRes = psGameAdapter.balance("terryzhang");
        log.info("balance: " + urlRes.getData());
    }

    @Test
    void findTicketStatus() {
        psGameAdapter.findTicketStatus("terryzhang", "20dbab1e8ce541eca5d52fe5ec8261b3");
    }

    @Test
    void transferIn() {
        Result<TransferStateVO> urlRes = psGameAdapter.transferIntoThird("terryzhang", BigDecimal.valueOf(2000l), UUID.randomUUID().toString());
        System.out.println("transferIn:" + urlRes.getData());
    }

    @Test
    void transferOut() {
        Result<TransferStateVO> urlRes = psGameAdapter.transferIntoPlatform("terryzhang", BigDecimal.valueOf(10l), UUID.randomUUID().toString());
        System.out.println("transferOut:" + urlRes.getData());
    }

    @Test
    void fetchBetOrder() {
        psGameAdapter.fetchBetOrder(LocalDateTime.now().minusHours(24), LocalDateTime.now());

    }

    @Test
    void doFetchBetOrderAction() {
        psGameAdapter.doFetchBetOrderAction();

    }
    @Autowired
    private  IMemberBetAmountRecordDailyService memberBetAmountRecordDailyService;
    @Autowired
    private  BetOrderMapper betOrderMapper;
    @Test
    void gggg() {
        LambdaQueryWrapper<MemberBetAmountRecordDaily> memberBetAmountRecordDailyLambdaQueryWrapper = new LambdaQueryWrapper<MemberBetAmountRecordDaily>()
                .gt(MemberBetAmountRecordDaily::getSettleTime, getTimeStart(-1))
                .le(MemberBetAmountRecordDaily::getSettleTime, getTimeStart(0));
        List<MemberBetAmountRecordDailyVO> dailyBetAmountForRebate = betOrderMapper.findDailyBetAmountForRebate(10, memberBetAmountRecordDailyLambdaQueryWrapper);
        List<List<MemberBetAmountRecordDailyVO>> partition = Lists.partition(dailyBetAmountForRebate, 1000);
        for (List<MemberBetAmountRecordDailyVO> memberBetAmountRecordDailies : partition) {
            // memberBetAmountRecordDailyService.saveBatch(memberBetAmountRecordDailies);
        }

    }
@Autowired
    private  BetOrderConverter betOrderConverter;
    @Test
    void insertfakeData() {

        for (int i=0;i<5;i++){

            BigDecimal validBet =new BigDecimal(8);
            BigDecimal settle = new BigDecimal(5);
            BigDecimal winLoss = validBet.subtract(settle);
            Integer status = BetOrderTypeEnum.BET_STATUS_SETTLED.getValue();
            BetOrderVO betOrder = new BetOrderVO();
            betOrder.setTransactionNo("111");
            betOrder.setTransactionSerial("111");
            betOrder.setTransactionTime(LocalDateTime.now());
            betOrder.setMemberId(118l);
            betOrder.setUsername(UUID.randomUUID().toString());
            betOrder.setPlatformId(29l);
            betOrder.setGameId("123");
            betOrder.setGameName("123");
            betOrder.setPlatformCode("KA");
            betOrder.setGameCategoryCode("chess");
            betOrder.setValidBetAmount(validBet);
            betOrder.setBetAmount(validBet);
            betOrder.setSettle(settle);
            betOrder.setSettleTime(LocalDateTime.now());
            betOrder.setWinLoss(winLoss);
            betOrder.setBetState(status);
            betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
            BetOrder betOrders = betOrderConverter.toEntity(betOrder);
            betOrderMapper.insert(betOrders);
        }


    }

    private LocalDateTime getTimeStart(Integer i){
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE,i);
        now.set(Calendar.HOUR_OF_DAY,0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        Instant instant = now.getTime().toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

}
