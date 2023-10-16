package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "遊戲輸贏報表-遊戲種類")
public class GameCategoryTypeReportVO {

    @Schema(title = "遊戲種類名稱", description = "名稱為i18n")
    private String gameCategoryName;

    @Schema(title = "遊戲種類名稱", description = "明細")
    private List<GameCategoryTypeDetailReportVO> gameCategoryTypeDetails;

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
