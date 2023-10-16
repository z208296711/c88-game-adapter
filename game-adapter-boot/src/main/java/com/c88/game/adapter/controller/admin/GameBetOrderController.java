package com.c88.game.adapter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.form.BetAmountRecordForm;
import com.c88.game.adapter.pojo.form.BetOrderRecordDetailForm;
import com.c88.game.adapter.pojo.form.BetOrderRecordForm;
import com.c88.game.adapter.pojo.vo.BetOrderDetailRecordVO;
import com.c88.game.adapter.pojo.vo.BetOrderRecordVO;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import com.c88.game.adapter.pojo.vo.TotalCountVO;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.vo.ReportBetOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "注單相關")
@RequiredArgsConstructor
@RequestMapping("/api/v1/bet/order")
public class GameBetOrderController {

    private final IBetOrderService iBetOrderService;

    @Operation(summary = "取得用戶歷史注單")
    @GetMapping("/{memberId}")
    public Result<BigDecimal> findTotalBet(@PathVariable Long memberId,
                                           @RequestParam(required = false) String platformCode,
                                           @RequestParam(required = false) String gameName,
                                           @RequestParam(required = false) String gameCategoryCode,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime) {

        BetOrder betOrder = iBetOrderService.getBaseMapper()
                .selectOne(new QueryWrapper<BetOrder>()
                        .select("sum(valid_bet_amount) as validBetAmount")
                        .lambda()
                        .eq(BetOrder::getMemberId, memberId)
                        .eq(StringUtils.isNotBlank(platformCode), BetOrder::getPlatformCode, platformCode)
                        .eq(StringUtils.isNotBlank(gameName), BetOrder::getGameName, gameName)
                        .eq(StringUtils.isNotBlank(gameCategoryCode), BetOrder::getGameCategoryCode, gameCategoryCode)
                        .ge(fromTime != null, BetOrder::getTransactionTime, fromTime)
                        .le(toTime != null, BetOrder::getTransactionTime, toTime)
                );
        return Result.success(betOrder == null ? BigDecimal.ZERO : betOrder.getValidBetAmount());
    }

    @Operation(summary = "投注記錄")
    @GetMapping("/record")
    public PageResult<BetOrderRecordVO> getBetOrderRecord(@ParameterObject BetOrderRecordForm form) {
        IPage<BetOrderRecordVO> betOrderRecord = iBetOrderService.getBetOrderRecord(form);
        List<TotalCountVO> totalCount = iBetOrderService.getTotalCount(form);
        return PageResult.success(betOrderRecord, totalCount);
    }

    @Operation(summary = "投注記錄詳情")
    @GetMapping("/record/detail")
    public PageResult<BetOrderDetailRecordVO> getBetOrderRecordDetail(@ParameterObject BetOrderRecordDetailForm form) {
        return PageResult.success(iBetOrderService.getBetOrderRecordDetail(form));
    }

    @Operation(summary = "公司輸贏報表使用")
    @GetMapping("/report/company")
    public Result<ReportBetOrderVO> getBerOrderCount(@RequestParam() String startTime, @RequestParam() String endTime){
        return Result.success(iBetOrderService.getBerOrderCount(startTime, endTime));
    }

    @Operation(summary = "公司輸贏報表結算使用")
    @GetMapping("/report/company/settle")
    public Result<ReportBetOrderVO> getBerOrderCountBySettleDate(@RequestParam() String startTime, @RequestParam() String endTime){
        return Result.success(iBetOrderService.getBerOrderCountBySettleDate(startTime, endTime));
    }

    @Operation(summary = "多玩家查詢下注額")
    @GetMapping("/report/betSum")
    public Result<List<Map<String,Object>>> getBetSumByMemberIds(@RequestParam() List<Long> memberIds, @RequestParam()  String startTime, @RequestParam() String endTime){
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        return Result.success(iBetOrderService.findBetSumByMemberIds(memberIds,start,end));
    }

    @Operation(summary = "後台-會員詳情-平台投入量-查詢")
    @GetMapping("/report/betAmount")
    public PageResult<MemberBetAmountRecordDailyVO> findDailyBetAmount(@ParameterObject BetAmountRecordForm form) {
        return PageResult.success(iBetOrderService.findDailyBetAmount(form));
    }
}
