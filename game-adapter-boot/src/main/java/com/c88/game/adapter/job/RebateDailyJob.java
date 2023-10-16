package com.c88.game.adapter.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.c88.game.adapter.mapper.BetOrderMapper;
import com.c88.game.adapter.pojo.entity.CategoryRebateRecord;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.entity.MemberRebateRecord;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import com.c88.game.adapter.service.ICategoryRebateRecordService;
import com.c88.game.adapter.service.IMemberBetAmountRecordDailyService;
import com.c88.game.adapter.service.IMemberRebateRecordService;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberRebateConfigDTO;
import com.c88.member.dto.MemberVipConfigDTO;
import com.c88.payment.dto.AddBalanceDTO;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.BONUS;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.REBATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class RebateDailyJob {

    private final IMemberBetAmountRecordDailyService memberBetAmountRecordDailyService;

    private final IMemberRebateRecordService memberRebateRecordService;

    private final ICategoryRebateRecordService categoryRebateRecordService;

    private final MemberFeignClient memberFeignClient;

    private final BetOrderMapper betOrderMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * CMD維護啟用
     */
    @XxlJob("rebateDailyJob")
    public void rebateDailyJob() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Integer, Map<Integer, List<MemberRebateConfigDTO>>> rebateConfigMap =
                memberFeignClient.getMemberRebateAll().getData()
                        .stream()
                        .collect(Collectors.groupingBy(MemberRebateConfigDTO::getCategoryId
                                , Collectors.groupingBy(MemberRebateConfigDTO::getVipId)));
        Map<Integer, String> memberVipConfigMap = memberFeignClient.getMemberVipConfigAll().getData().stream().collect(Collectors.toMap(MemberVipConfigDTO::getId, MemberVipConfigDTO::getDailyBackwaterLimit, (o, n) -> n));
        //calculate rebate
        List<MemberRebateRecord> rebateRecords = new ArrayList<>();
        List<CategoryRebateRecord> categoryRebateRecords = new ArrayList<>();
        List<MemberBetAmountRecordDailyVO> unRebateLists = memberBetAmountRecordDailyService.getUnRebateLists(Map.of("settleTime", LocalDateTime.of(LocalDate.now(), LocalTime.MIN)));
        for (MemberBetAmountRecordDailyVO unRebate : unRebateLists) {
            if (Objects.isNull(unRebate.getGameCategoryCode())|| Objects.isNull(unRebate.getVipId())) {
                continue;
            }
            MemberRebateConfigDTO rebateConfig = rebateConfigMap.get(Integer.parseInt(unRebate.getGameCategoryCode())).get(unRebate.getVipId()).get(0);
            BigDecimal rebateRate = BigDecimal.valueOf(rebateConfig.getRebate()).divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN);
            BigDecimal rebateMoney = unRebate.getValidBetAmount().multiply(rebateRate).setScale(2, RoundingMode.DOWN);
            // write record
            MemberRebateRecord rebateRecord = MemberRebateRecord.builder()
                    .memberId(unRebate.getMemberId())
                    .rebate(rebateMoney)
                    .settle(unRebate.getSettle())
                    .vipId(unRebate.getVipId())
                    .username(unRebate.getUsername())
                    .betAmount(unRebate.getBetAmount())
                    .validBetAmount(unRebate.getValidBetAmount())
                    .vipName(unRebate.getVipName())
                    .betTimes(unRebate.getBetTimes())
                    .betAmount(unRebate.getBetAmount())
                    .build();
            rebateRecords.add(rebateRecord);

            CategoryRebateRecord categoryRebateRecord = CategoryRebateRecord.builder()
                    .memberId(unRebate.getMemberId())
                    .rebate(rebateMoney)
                    .settle(unRebate.getSettle())
                    .betAmount(unRebate.getBetAmount())
                    .validBetAmount(unRebate.getValidBetAmount())
                    .betTimes(unRebate.getBetTimes())
                    .betAmount(unRebate.getBetAmount())
                    .gmtCreate(LocalDateTime.now())
                    .gameCategoryCode(unRebate.getGameCategoryCode())
                    .build();
            categoryRebateRecords.add(categoryRebateRecord);
        }
        //統計遊戲分類的反水
        if(CollectionUtils.isNotEmpty(categoryRebateRecords)){
            categoryRebateRecordService.insertBatchXml(categoryRebateRecords);
        }

        //polymerization data
        rebateRecords = rebateRecords.stream()
                .collect(Collectors.collectingAndThen(Collectors.groupingBy(MemberRebateRecord::getMemberId,
                         Collectors.collectingAndThen(Collectors.toList(), c -> {
                             BigDecimal betAmountSum = c.stream().map(MemberRebateRecord::getBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
                             BigDecimal validBetSum = c.stream().map(MemberRebateRecord::getValidBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
                             BigDecimal settleSum = c.stream().map(MemberRebateRecord::getSettle).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
                             Integer betTimes = c.stream().map(MemberRebateRecord::getBetTimes).reduce(0, Integer::sum);
                             BigDecimal rebate = c.stream().map(MemberRebateRecord::getRebate).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
                             Long memberId = c.stream().map(MemberRebateRecord::getMemberId).findFirst().get();
                             Integer vipId = c.stream().map(MemberRebateRecord::getVipId).findFirst().get();
                             String vipName = c.stream().map(MemberRebateRecord::getVipName).findFirst().get();
                             String userName = c.stream().map(MemberRebateRecord::getUsername).findFirst().get();
                                   return MemberRebateRecord.builder()
                                                            .memberId(memberId)
                                                            .rebate(rebate)
                                                            .settle(settleSum)
                                                            .vipId(vipId)
                                                            .username(userName)
                                                            .betAmount(betAmountSum)
                                                            .validBetAmount(validBetSum)
                                                            .vipName(vipName)
                                                            .betTimes(betTimes)
                                                            .gmtCreate(getTimeStart(0,6))
                                                            .build();
                                                }
                                        )
                                ), m -> new ArrayList<>(m.values())
                        )
                );

        for (MemberRebateRecord rebateRecord : rebateRecords) {
            //send queue
            BigDecimal rebateLimit = new BigDecimal(memberVipConfigMap.get(rebateRecord.getVipId()));
            //send queue
            BigDecimal validRebate = rebateRecord.getRebate().compareTo(rebateLimit) >= 0 ? rebateLimit : rebateRecord.getRebate();
            rebateRecord.setRebate(validRebate);
            if (rebateRecord.getRebate().compareTo(BigDecimal.ZERO)!=0){
                 kafkaTemplate.send(BALANCE_CHANGE,
                    AddBalanceDTO.builder()
                            .memberId(rebateRecord.getMemberId())
                            .balance(validRebate)
                            .balanceChangeTypeLinkEnum(REBATE)
                            .type(REBATE.getType())
                            .betRate(BigDecimal.ZERO)
                            .note(REBATE.getI18n())
                            .gmtCreate(rebateRecord.getGmtCreate()).build());
            }
        }
        //insert member_rebate_record
        if (CollectionUtils.isNotEmpty(rebateRecords)) {
            List<List<MemberRebateRecord>> partition = Lists.partition(rebateRecords, 1000);
            for (List<MemberRebateRecord> memberRebateRecords : partition) {
                memberRebateRecordService.insertBatchXml(memberRebateRecords);
            }
        }
        // update status
        memberBetAmountRecordDailyService.lambdaUpdate()
                .eq(MemberBetAmountRecordDaily::getRebateStatus, 0)
                .set(MemberBetAmountRecordDaily::getRebateStatus, 1)
                .update();

        stopwatch.stop();
        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        log.info("rebateDailyJob_executeTime: " + millis + " ms");
    }

    @XxlJob("memberBetAmountDailyJob")
    public void memberBetAmountDailyJob() {
        // loop 0-9 bet_order
        LambdaQueryWrapper<MemberBetAmountRecordDaily> memberBetAmountRecordDailyLambdaQueryWrapper = new LambdaQueryWrapper<MemberBetAmountRecordDaily>()
                .gt(MemberBetAmountRecordDaily::getSettleTime, getTimeStart(-1,16))
                .le(MemberBetAmountRecordDaily::getSettleTime, getTimeStart(0,16));
        Stopwatch stopwatch = Stopwatch.createStarted();
        IntStream.range(0, 10).forEach(i -> {
            List<MemberBetAmountRecordDailyVO> dailyBetAmountForRebate = betOrderMapper.findDailyBetAmountForRebate(i, memberBetAmountRecordDailyLambdaQueryWrapper);
            List<List<MemberBetAmountRecordDailyVO>> partition = Lists.partition(dailyBetAmountForRebate, 1000);
            for (List<MemberBetAmountRecordDailyVO> memberBetAmountRecordDailies : partition) {
                memberBetAmountRecordDailyService.insertBatchXml(memberBetAmountRecordDailies);
            }
        });
        stopwatch.stop();
        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        log.info("memberBetAmountDailyJob_executeTime: " + millis + " ms");

    }

    private static LocalDateTime getTimeStart(Integer d,Integer h) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, d);
        now.set(Calendar.HOUR_OF_DAY, h);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        Instant instant = now.getTime().toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }


}
