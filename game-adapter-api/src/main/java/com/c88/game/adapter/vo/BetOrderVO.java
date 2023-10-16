package com.c88.game.adapter.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 會員注單紀錄
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetOrderVO {

    /**
     * 注單ID
     */
    private String transactionNo;

    /**
     * 注單號
     */
    private String transactionSerial;

    /**
     * 交易時間
     */
    private LocalDateTime transactionTime;

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * 會員名稱
     */
    private String username;

    /**
     * 三方平台ID
     */
    private Long platformId;

    /**
     * 三方平台代碼
     */
    private String platformCode;

    /**
     * 三方的遊戲ID
     */
    private String gameId;

    /**
     * 三方的遊戲名稱
     */
    private String gameName;

    /**
     * 遊戲類型
     */
    private String gameCategoryCode;

    /**
     * 該筆有效投注額
     */
    private BigDecimal validBetAmount;

    /**
     * 該筆投注額
     */
    private BigDecimal betAmount;

    /**
     * 該筆總派彩
     */
    private BigDecimal settle;

    /**
     * 派彩時間
     */
    private LocalDateTime settleTime;

    /**
     * 該筆總輸贏
     */
    private BigDecimal winLoss;

    /**
     * 該筆注單狀態 0:未派彩, 1:已派彩
     */
    private Integer betState;

    /**
     * 反水計算狀態 0:未結算, 1:已結算
     */
    private Integer rebateState;


    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private Integer settleNote;
}
