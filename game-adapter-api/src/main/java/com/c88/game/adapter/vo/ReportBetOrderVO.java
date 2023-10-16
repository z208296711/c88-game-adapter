package com.c88.game.adapter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ReportBetOrderVO {
    @Schema(title = "投注人數")
    Integer betCount;

    @Schema(title = "投注帳號")
    List<Long> betMembers;

    @Schema(title = "總投注額")
    BigDecimal allBetAmount;

    @Schema(title = "有效投注額")
    BigDecimal allValidBetAmount;

//    @Schema(title = "公司輸贏")
//    BigDecimal companyWinLoss;

    @Schema(title = "派彩金額")
    BigDecimal settleAmount;

}
