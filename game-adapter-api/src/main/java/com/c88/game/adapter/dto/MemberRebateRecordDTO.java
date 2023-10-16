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
 * @TableName member_rebate_record
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRebateRecordDTO implements Serializable {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private Long memberId;

    /**
     * 
     */
    private String username;

    /**
     * 
     */
    private String vipName;

    /**
     * 
     */
    transient
    private String parentName;

    /**
     * 
     */
    private Integer vipId;

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
     * 反水金額
     */
    private BigDecimal rebate;

    private static final long serialVersionUID = 1L;
}
