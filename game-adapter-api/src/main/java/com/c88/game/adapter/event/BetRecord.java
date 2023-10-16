package com.c88.game.adapter.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BetRecord {

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
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime settleTime;

    /**
     * 該筆總輸贏
     */
    private BigDecimal winLoss;

    /**
     * 該筆注單狀態 0:未派彩, 1:已派彩
     */
    private Integer betState;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime gmtModified;

    /**
     * 注單狀態事件
     */
    private Integer eventType;

    /**
     * 差額
     */
    private BigDecimal settleDiff;

    /**
     * 最後提款完成時間
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime lastWithdraw;

    public BetRecord(LocalDateTime lastWithdraw) {
        this.lastWithdraw = lastWithdraw;
    }

    public BetRecord() {

    }
}
