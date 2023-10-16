package com.c88.game.adapter.pojo.document;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ES會員注單紀錄
 */
@Data
@Document(indexName = "bet-order", createIndex = false)
public class BetOrderDocument {

    /**
     * 注單ID
     */
    @Id
    private String transactionNo;

    /**
     * 注單號
     */
    @Field(type = FieldType.Keyword)
    private String transactionSerial;

    /**
     * 交易時間
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionTime;

    /**
     * 會員ID
     */
    @Field(type = FieldType.Keyword)
    private Long memberId;

    /**
     * 會員ID
     */
    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private String username;

    /**
     * 三方平台ID
     */
    @Field(type = FieldType.Keyword)
    private Long platformId;

    /**
     * 三方平台代碼
     */
    @Field(type = FieldType.Keyword)
    private String platformCode;

    /**
     * 三方的遊戲ID
     */
    @Field(type = FieldType.Keyword)
    private String gameId;

    /**
     * 三方的遊戲名稱
     */
    @Field(type = FieldType.Keyword)
    private String gameName;

    /**
     * 遊戲類型
     */
    @TableField(value = "game_category_code")
    private String gameCategoryCode;

    /**
     * 該筆有效投注額
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal validBetAmount;

    /**
     * 該筆投注額
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal betAmount;

    /**
     * 該筆總派彩
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal settle;

    /**
     * 派彩時間
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime settleTime;

    /**
     * 該筆總輸贏
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal winLoss;

    /**
     * 該筆注單狀態 0:未派彩, 1:已派彩
     */
    @Field
    private Integer betState;

    /**
     * 反水計算狀態 0:未結算, 1:已結算
     */
    @Field
    private Integer rebateState;

}