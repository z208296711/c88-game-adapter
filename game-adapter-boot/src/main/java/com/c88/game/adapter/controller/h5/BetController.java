package com.c88.game.adapter.controller.h5;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.core.enums.AccountRecordTimeTypeEnum;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.MemberUtils;
import com.c88.game.adapter.mapper.BetOrderMapper;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.form.FindMemberAccountBetRecordForm;
import com.c88.game.adapter.pojo.vo.H5MemberAccountBetDetailRecordVO;
import com.c88.game.adapter.pojo.vo.H5MemberAccountBetPlatformDetailRecordVO;
import com.c88.game.adapter.pojo.vo.H5MemberAccountBetRecordVO;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.service.IPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "投注相關")
@RequiredArgsConstructor
@RequestMapping("/h5/bet")
public class BetController {

    private final IBetOrderService iBetOrderService;
    private final BetOrderMapper betOrderMapper;

    private final IPlatformService iPlatformService;

    @Operation(summary = "帳務紀錄-投注", description = "投注")
    @GetMapping(value = "/member/account/bet/record")
    public PageResult<H5MemberAccountBetDetailRecordVO> findMemberAccountBetRecord(@Validated @ParameterObject FindMemberAccountBetRecordForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        // 取的過濾後的總金額
        Map<String, Object> totalBetOrderMap = Optional.ofNullable(iBetOrderService.getMap(
                                Wrappers.<BetOrder>query()
                                        .select("sum(bet_amount) as betAmount", "sum(valid_bet_amount) as validBetAmount", "sum(win_loss) as winLoss")
                                        .lambda()
                                        .eq(BetOrder::getMemberId, memberId)
                                        .eq(StringUtils.isNotBlank(form.getGameCategoryCode()), BetOrder::getGameCategoryCode, form.getGameCategoryCode())
                                        .eq(StringUtils.isNotBlank(form.getPlatformCode()), BetOrder::getPlatformCode, form.getPlatformCode())
                                        .between(BetOrder::getTransactionTime, AccountRecordTimeTypeEnum.getStartDateTime(form.getTimeType()), AccountRecordTimeTypeEnum.getEndDateTime(form.getTimeType()))
                                        .groupBy(BetOrder::getMemberId)
                        )
                )
                .orElse(Map.of());

        // 取得會員投注概要(因細項資料無法確認資料筆數所以須先取得概要)
        Page<H5MemberAccountBetDetailRecordVO> betRecords = betOrderMapper.findMemberAccountBetRecordPlatformDate(new Page<>(form.getPageNum(),
                        form.getPageSize()),
                memberId,
                AccountRecordTimeTypeEnum.getStartDateTime(form.getTimeType()),
                AccountRecordTimeTypeEnum.getEndDateTime(form.getTimeType()),
                form);

        // 取得會員投注概要細項
        List<BetOrder> betOrders = iBetOrderService.lambdaQuery()
                .eq(BetOrder::getMemberId, memberId)
                .eq(StringUtils.isNotBlank(form.getGameCategoryCode()), BetOrder::getGameCategoryCode, form.getGameCategoryCode())
                .eq(StringUtils.isNotBlank(form.getPlatformCode()), BetOrder::getPlatformCode, form.getPlatformCode())
                .between(BetOrder::getTransactionTime, AccountRecordTimeTypeEnum.getStartDateTime(form.getTimeType()), AccountRecordTimeTypeEnum.getEndDateTime(form.getTimeType()))
                .list();

        // 取得平台名稱
        Map<String, String> platformNameMap = iPlatformService.lambdaQuery()
                .select(Platform::getCode, Platform::getName)
                .list()
                .stream()
                .collect(Collectors.toMap(Platform::getCode, Platform::getName));

        // 將會員投注細項寫入概要
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        betRecords.convert(betRecord -> {
                    List<H5MemberAccountBetPlatformDetailRecordVO> betOrdersByPlatformDate = betOrders.stream()
                            .filter(filter ->
                                    filter.getPlatformCode().equals(platformNameMap.getOrDefault(betRecord.getPlatformName(), "")) &&
                                            filter.getTransactionTime().format(formatDate).equals(betRecord.getCreateDate().format(formatDate)) &&
                                            filter.getGameCategoryCode().equals(betRecord.getGameCategoryCode())
                            )
                            .map(betOrder ->
                                    H5MemberAccountBetPlatformDetailRecordVO.builder()
                                            .gameName(betOrder.getGameName())
                                            .betAmount(betOrder.getBetAmount())
                                            .validBetAmount(betOrder.getValidBetAmount())
                                            .winLoss(betOrder.getWinLoss())
                                            .transactionSerial(betOrder.getTransactionSerial())
                                            .betState(betOrder.getBetState())
                                            .note(betOrder.getSettleNote())
                                            .createTime(betOrder.getTransactionTime().toLocalTime())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    return H5MemberAccountBetDetailRecordVO.builder()
                            .gameCategoryCode(betRecord.getGameCategoryCode())
                            .platformName(platformNameMap.getOrDefault(betRecord.getPlatformName(), ""))
                            .validBetAmount(betOrdersByPlatformDate.stream().map(H5MemberAccountBetPlatformDetailRecordVO::getValidBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .winLoss(betOrdersByPlatformDate.stream().map(H5MemberAccountBetPlatformDetailRecordVO::getWinLoss).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .createDate(betRecord.getCreateDate())
                            .platformDetail(betOrdersByPlatformDate)
                            .build();
                }
        );

        return PageResult.success(betRecords,
                H5MemberAccountBetRecordVO.builder()
                        .betAmount((BigDecimal) totalBetOrderMap.getOrDefault("betAmount", BigDecimal.ZERO))
                        .validBetAmount((BigDecimal) totalBetOrderMap.getOrDefault("validBetAmount", BigDecimal.ZERO))
                        .winLoss(((BigDecimal) totalBetOrderMap.getOrDefault("winLoss", BigDecimal.ZERO)))
                        .build()
        );
    }

}
