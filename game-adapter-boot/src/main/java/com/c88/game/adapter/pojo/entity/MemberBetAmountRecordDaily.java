package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @TableName member_bet_amount_record_daily
 */
@TableName(value = "member_bet_amount_record_daily")
@Data
public class MemberBetAmountRecordDaily extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 會員ID
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 會員帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 平台代碼
     */
    @TableField(value = "platform_code")
    private String platformCode;

    /**
     * 平台id
     */
    @TableField(value = "platform_id")
    private Integer platformId;

    /**
     * 遊戲類型
     */
    @TableField(value = "game_category_code")
    private String gameCategoryCode;

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
     * 該筆總輸贏
     */
    @TableField(value = "win_loss")
    private BigDecimal winLoss;

    /**
     * 返水狀態
     */
    @TableField(value = "rebate_status")
    private BigDecimal rebateStatus;

    /**
     * 派彩日期
     */
    @TableField(value = "settle_time")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settleTime;

    /**
     * 投注次數
     */
    @TableField(value = "bet_times")
    private Integer betTimes;

    @TableField(value = "gmt_create")
    private LocalDateTime gmtCreate;

}
