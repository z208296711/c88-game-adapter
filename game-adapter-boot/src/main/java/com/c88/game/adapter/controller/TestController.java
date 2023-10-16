package com.c88.game.adapter.controller;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.AuthUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test/bet")
@Profile("!k8s_prod & !k8s_pre")
@Tag(name = "測試各項功能")
public class TestController {

    private final MemberFeignClient memberFeignClient;
    private final IBetOrderService iBetOrderService;
    private final GameAdapterExecutor gameAdapterExecutor;
    private final IPlatformService iPlatformService;

    // @GetMapping("/terry")
    // public void getTerry(){
    //     // loop 0-9 bet_order
    //     LambdaQueryWrapper<MemberBetAmountRecordDaily> memberBetAmountRecordDailyLambdaQueryWrapper = new LambdaQueryWrapper<MemberBetAmountRecordDaily>();
    //     Stopwatch stopwatch = Stopwatch.createStarted();
    //     IntStream.range(0, 9).forEach(i -> {
    //         List<MemberBetAmountRecordDaily> dailyBetAmountForRebate = betOrderMapper.findDailyBetAmountForRebate(i, memberBetAmountRecordDailyLambdaQueryWrapper);
    //         List<List<MemberBetAmountRecordDaily>> partition = Lists.partition(dailyBetAmountForRebate, 1000);
    //         for (List<MemberBetAmountRecordDaily> memberBetAmountRecordDailies : partition) {
    //             memberBetAmountRecordDailyService.insertBatchXml(memberBetAmountRecordDailies);
    //         }
    //     });
    //     stopwatch.stop();
    //     long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
    //     log.info("memberBetAmountDailyJob_executeTime: " + millis + " ms");
    //
    //
    //     Map<Integer, Map<Integer, List<MemberRebateConfigDTO>>> rebateConfigMap =
    //             memberFeignClient.getMemberRebateAll().getData()
    //                     .stream()
    //                     .collect(Collectors.groupingBy(MemberRebateConfigDTO::getCategoryId
    //                             , Collectors.groupingBy(MemberRebateConfigDTO::getVipId)));
    //     Map<Integer, String> memberVipConfigMap = memberFeignClient.getMemberVipConfigAll().getData().stream().collect(Collectors.toMap(MemberVipConfigDTO::getId, MemberVipConfigDTO::getDailyBackwaterLimit, (o, n) -> n));
    //     //calculate rebate
    //     ArrayList<MemberRebateRecord> rebateRecords = new ArrayList<>();
    //     List<MemberBetAmountRecordDaily> unRebateLists = memberBetAmountRecordDailyService.getUnRebateLists(Map.of("settleTime", LocalDateTime.of(LocalDate.now(), LocalTime.MIN)));
    //     for (MemberBetAmountRecordDaily unRebate : unRebateLists) {
    //         if (Objects.isNull(unRebate.getGameCategoryCode())|| Objects.isNull(unRebate.getVipId())) {
    //             continue;
    //         }
    //         MemberRebateConfigDTO rebateConfig = rebateConfigMap.get(Integer.parseInt(unRebate.getGameCategoryCode())).get(unRebate.getVipId()).get(0);
    //         BigDecimal rebateRate = BigDecimal.valueOf(rebateConfig.getRebate()).divide(BigDecimal.valueOf(100), 3, RoundingMode.DOWN);
    //         BigDecimal rebateMoney = unRebate.getValidBetAmount().multiply(rebateRate).setScale(0, RoundingMode.DOWN);
    //         // write record
    //         MemberRebateRecord rebateRecord = MemberRebateRecord.builder()
    //                 .memberId(unRebate.getMemberId())
    //                 .rebate(rebateMoney)
    //                 .settle(unRebate.getSettle())
    //                 .vipId(unRebate.getVipId())
    //                 .username(unRebate.getUsername())
    //                 .betAmount(unRebate.getBetAmount())
    //                 .validBetAmount(unRebate.getValidBetAmount())
    //                 .vipName(unRebate.getVipName())
    //                 .betTimes(unRebate.getBetTimes())
    //                 .betAmount(unRebate.getBetAmount())
    //                 .build();
    //         rebateRecords.add(rebateRecord);
    //     }
    //     //polymerization data
    //     rebateRecords = rebateRecords.stream()
    //             .collect(Collectors.collectingAndThen(Collectors.groupingBy(MemberRebateRecord::getMemberId,
    //                                     Collectors.collectingAndThen(Collectors.toList(), c -> {
    //                                                 BigDecimal betAmountSum = c.stream().map(MemberRebateRecord::getBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    //                                                 BigDecimal validBetSum = c.stream().map(MemberRebateRecord::getValidBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    //                                                 BigDecimal settleSum = c.stream().map(MemberRebateRecord::getSettle).reduce(BigDecimal.ZERO, BigDecimal::add);
    //                                                 Integer betTimes = c.stream().map(MemberRebateRecord::getBetTimes).reduce(0, Integer::sum);
    //                                                 BigDecimal rebate = c.stream().map(MemberRebateRecord::getRebate).reduce(BigDecimal.ZERO, BigDecimal::add);
    //                                                 Long memberId = c.stream().map(MemberRebateRecord::getMemberId).findFirst().get();
    //                                                 Integer vipId = c.stream().map(MemberRebateRecord::getVipId).findFirst().get();
    //                                                 String vipName = c.stream().map(MemberRebateRecord::getVipName).findFirst().get();
    //                                                 String userName = c.stream().map(MemberRebateRecord::getUsername).findFirst().get();
    //                                                 return MemberRebateRecord.builder()
    //                                                         .memberId(memberId)
    //                                                         .rebate(rebate)
    //                                                         .settle(settleSum)
    //                                                         .vipId(vipId)
    //                                                         .username(userName)
    //                                                         .betAmount(betAmountSum)
    //                                                         .validBetAmount(validBetSum)
    //                                                         .vipName(vipName)
    //                                                         .betTimes(betTimes)
    //                                                         .gmtCreate(LocalDateTime.now())
    //                                                         .build();
    //                                             }
    //                                     )
    //                             ), m -> new ArrayList<>(m.values())
    //                     )
    //             );
    //
    //     for (MemberRebateRecord rebateRecord : rebateRecords) {
    //         //send queue
    //         BigDecimal rebateLimit = new BigDecimal(memberVipConfigMap.get(rebateRecord.getVipId()));
    //         //send queue
    //         kafkaTemplate.send(BALANCE_CHANGE,
    //                 AddBalanceDTO.builder()
    //                         .memberId(rebateRecord.getMemberId())
    //                         .balance(rebateRecord.getRebate().compareTo(rebateLimit)>=0?rebateLimit:rebateRecord.getRebate())
    //                         .balanceChangeTypeLinkEnum(BONUS)
    //                         .type(BONUS.getType())
    //                         .betRate(BigDecimal.ZERO)
    //                         .note(BONUS.getI18n())
    //                         .gmtCreate(rebateRecord.getGmtCreate()).build());
    //     }
    //     //insert member_rebate_record
    //     if (CollectionUtils.isNotEmpty(rebateRecords)) {
    //         List<List<MemberRebateRecord>> partition = Lists.partition(rebateRecords, 1000);
    //         for (List<MemberRebateRecord> memberRebateRecords : partition) {
    //             memberRebateRecordService.insertBatchXml(memberRebateRecords);
    //         }
    //     }
    //     // update status
    //     memberBetAmountRecordDailyService.lambdaUpdate()
    //             .eq(MemberBetAmountRecordDaily::getRebateStatus, 0)
    //             .set(MemberBetAmountRecordDaily::getRebateStatus, 1)
    //             .update();
    //
    //     stopwatch.stop();
    //     log.info("rebateDailyJob_executeTime: " + millis + " ms");
    // }

    @Operation(summary = "取得注單")
    @GetMapping("/platform/code/{code}")
    public List<Object> findPlatformBetByCode(@PathVariable("code") String code) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(55);
        LocalDateTime endTime = LocalDateTime.now();
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(code);
        List<Object> Objects = currentGameAdapter.fetchBetOrder(startTime, endTime);
        return Objects;
    }

    @Operation(summary = "取得餘額")
    @GetMapping("/platform/balance/code/{code}/{username}")
    public BigDecimal balanceByCode(@PathVariable("code") String code,
                                    @PathVariable("username") String username) {
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(code);
        Result<BigDecimal> balance = currentGameAdapter.balance(username);
        return balance.getData();
    }

    @Operation(summary = "測試存錢")
    @GetMapping("/platform/transfer/in/code/{code}/{username}/{amount}")
    public TransferStateVO transferInByCode(@PathVariable("code") String code,
                                            @PathVariable("username") String username,
                                            @PathVariable("amount") BigDecimal amount) {
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(code);
        Result<TransferStateVO> transferState = currentGameAdapter.transferIntoThird(username, amount, UUID.fastUUID().toString(false));
        return transferState.getData();
    }

    @Operation(summary = "測試取錢")
    @GetMapping("/platform/transfer/out/code/{code}/{username}/{amount}")
    public TransferStateVO transferOutByCode(@PathVariable("code") String code,
                                             @PathVariable("username") String username,
                                             @PathVariable("amount") BigDecimal amount) {
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(code);
        Result<TransferStateVO> transferState = currentGameAdapter.transferIntoPlatform(username, amount, UUID.fastUUID().toString(false));
        return transferState.getData();
    }

    @Operation(summary = "註冊遊戲帳號")
    @GetMapping("/platform/register/{platformId}/{memberId}/{username}")
    public String registerByCode(@PathVariable("platformId") Long platformId,
                                 @PathVariable("memberId") Long memberId,
                                 @PathVariable("username") String username) {
        Platform platform = iPlatformService.getPlatformById(platformId);
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(platform.getCode());
        Result<String> register = currentGameAdapter.register(memberId, username, platform);
        return register.getData();
    }

    @Operation(summary = "取得登入網址")
    @GetMapping("/platform/login/{code}/{username}")
    public String findPlatformLoginURLByCode(@PathVariable("code") String code,
                                             @PathVariable("username") String username,
                                             @RequestParam Map<String, String> param) {
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(code);
        Result<String> urlResult = currentGameAdapter.login(username, param);
        return urlResult.getData();
    }

    @Operation(summary = "取得訂單狀態")
    @GetMapping("/platform/ticjet/status/{code}/{username}/{orderId}")
    public String findTicketStatusByCode(@PathVariable("code") String code,
                                         @PathVariable("username") String username,
                                         @PathVariable("orderId") String orderId) {
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(code);
        Result<String> urlResult = currentGameAdapter.findTicketStatus(username, orderId);
        return urlResult.getData();
    }

    @Operation(summary = "取得用戶歷史注單")
    @GetMapping("/{username}")
    public Result<BigDecimal> findTotalBet(@PathVariable String username,
                                           @RequestParam(required = false) String gameName,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime) {

        Result<AuthUserDTO> result = memberFeignClient.getMemberByUserName(username);
        if (!Result.isSuccess(result)) {
            return null;
        }

        BetOrder betOrder = iBetOrderService.getBaseMapper().selectOne(new QueryWrapper<BetOrder>()
                .select("sum(valid_bet_amount) as validBetAmount")
                .lambda()
                .eq(BetOrder::getMemberId, result.getData().getId())
                .eq(StringUtils.isNotBlank(gameName), BetOrder::getGameName, gameName)
                .gt(fromTime != null, BetOrder::getTransactionTime, fromTime)
                .lt(toTime != null, BetOrder::getTransactionTime, toTime));
        return Result.success(betOrder == null ? BigDecimal.ZERO : betOrder.getValidBetAmount());
    }

    @Operation(summary = "清除用戶歷史注單")
    @DeleteMapping("/{username}")
    public Result<Boolean> delTotalBet(@PathVariable String username,
                                       @RequestParam(required = false) String gameName,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime) {

        Result<AuthUserDTO> result = memberFeignClient.getMemberByUserName(username);
        if (!Result.isSuccess(result)) {
            return null;
        }

        iBetOrderService.getBaseMapper().delete(new QueryWrapper<BetOrder>()
                .lambda()
                .eq(BetOrder::getMemberId, result.getData().getId())
                .eq(StringUtils.isNotBlank(gameName), BetOrder::getGameName, gameName)
                .gt(fromTime != null, BetOrder::getTransactionTime, fromTime)
                .lt(toTime != null, BetOrder::getTransactionTime, toTime));
        return Result.success(true);
    }


}
