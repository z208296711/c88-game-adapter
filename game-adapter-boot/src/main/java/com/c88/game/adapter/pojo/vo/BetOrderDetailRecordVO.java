package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BetOrderDetailRecordVO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(title = "投注時間")
    LocalDateTime transactionTime;

    @Schema(title = "注單編號")
    String transactionNo;

    @Schema(title = "平台名稱")
    String platform;

    @Schema(title = "遊戲類型")
    String category;

    @Schema(title = "遊戲名稱")
    String gameName;

    @Schema(title = "英文遊戲名稱")
    String gameNameEN;

    @Schema(title = "投注前餘額")
    BigDecimal betAmountBefore;

    @Schema(title = "投注額")
    BigDecimal betAmount;

    @Schema(title = "派彩")
    BigDecimal settle;

    @Schema(title = "輸贏")
    BigDecimal winLoss;

    @Schema(title = "有效投注額")
    BigDecimal validBetAmount;

    @Schema(title = "投注後餘額")
    BigDecimal betAmountAfter;

    @Schema(title = "詳情")
    String detail;

    @Schema(title = "狀態 0:未結算, 1:已結算")
    int betState;

    @Schema(title = "狀態 1已取消 2退款投注 3兌現")
    int settleNote;




}
