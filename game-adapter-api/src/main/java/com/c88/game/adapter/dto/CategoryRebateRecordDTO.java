package com.c88.game.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 
 * @TableName category_rebate_record
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRebateRecordDTO implements Serializable {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private Long memberId;

    /**
     * 投注次數
     */
    private Integer betTimes;

    /**
     * 
     */
    private LocalDate gmtCreate;

    /**
     * 該筆投注額
     */
    private BigDecimal betAmount;

    /**
     * 該筆有效投注額
     */
    private BigDecimal validBetAmount;

    /**
     * 該筆總派彩
     */
    private BigDecimal settle;

    /**
     * 遊戲類型
     */
    private String gameCategoryCode;

    /**
     * 反水金額
     */
    private BigDecimal rebate;

    private static final long serialVersionUID = 1L;
}
