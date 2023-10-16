package com.c88.game.adapter.pojo.vo;

import com.c88.game.adapter.pojo.entity.MemberRebateRecord;
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
@Schema(title = "反水匯總")
public class MemberRebateRecordTotalVO {

    @Schema(title = "投注次數")
    private Integer betTimes;
    @Schema(title = "下注金額")
    private BigDecimal betAmount;
    @Schema(title = "派彩金額")
    private BigDecimal settle;
    @Schema(title = "有效投注")
    private BigDecimal validBetAmount;
    @Schema(title = "返水金額")
    private BigDecimal rebate;
}
