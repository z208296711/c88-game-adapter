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
@Schema(title = "遊戲輸贏報表")
public class GameWinLossReportVO {

    @Schema(title = "遊戲種類")
    private List<GameCategoryTypeReportVO> gameCategoryTypes;

    @Schema(title = "總投注人數")
    private Long totalBetManCount;

    @Schema(title = "總投注筆數")
    private Long totalBetCount;

    @Schema(title = "總有效投注金額")
    private BigDecimal totalValidBetAmount;

    @Schema(title = "總輸贏")
    private BigDecimal totalWinLoss;

    @Schema(title = "總返水")
    private BigDecimal totalRebate;

}
