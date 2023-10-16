package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "帳務投注記錄")
public class H5MemberAccountBetRecordVO {

    @Schema(title = "投注額")
    private BigDecimal betAmount;

    @Schema(title = "有效投注")
    private BigDecimal validBetAmount;

    @Schema(title = "輸贏")
    private BigDecimal winLoss;

}
