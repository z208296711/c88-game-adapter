package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName member_rebate_record
 */
@TableName(value ="member_rebate_record")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRebateRecord implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 
     */
    @TableField(value = "username")
    private String username;

    /**
     * 
     */
    @TableField(value = "vip_name")
    private String vipName;

    /**
     * 
     */
    transient
    private String parentName;

    /**
     * 
     */
    @TableField(value = "vip_id")
    private Integer vipId;

    /**
     * 投注次數
     */
    @TableField(value = "bet_times")
    private Integer betTimes;

    /**
     * 
     */
    @TableField(value = "gmt_create")
    private LocalDateTime gmtCreate;

    /**
     * 該筆投注額
     */
    @TableField(value = "bet_amount")
    private BigDecimal betAmount;

    /**
     * 該筆有效投注額
     */
    @TableField(value = "valid_bet_amount")
    private BigDecimal validBetAmount;

    /**
     * 該筆總派彩
     */
    @TableField(value = "settle")
    private BigDecimal settle;

    /**
     * 反水金額
     */
    @TableField(value = "rebate")
    private BigDecimal rebate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
