package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BetOrderRecordVO {
    @Schema(title = "平台名稱")
    String platform;

    @Schema(title = "遊戲類型")
    String category;

    @Schema(title = "有效投注")
    BigDecimal validBet;

    @Schema(title = "總投注")
    BigDecimal totalBet;

    @Schema(title = "總輸贏")
    BigDecimal totalWinLoss;
}
