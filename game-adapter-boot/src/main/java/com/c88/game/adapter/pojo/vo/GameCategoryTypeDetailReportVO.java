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
@Schema(title = "遊戲輸贏-遊戲種類-明細")
public class GameCategoryTypeDetailReportVO {

    @Schema(title = "遊戲種類名稱", description = "平台名稱:類型(i18n)")
    private String gameCategoryName;

    @Schema(title = "投注人數")
    private Long betManCount;

    @Schema(title = "投注筆數")
    private Long betCount;

    @Schema(title = "有效投注金額")
    private BigDecimal validBetAmount;

    @Schema(title = "輸贏")
    private BigDecimal winLoss;

    @Schema(title = "返水")
    private BigDecimal rebate;

}
