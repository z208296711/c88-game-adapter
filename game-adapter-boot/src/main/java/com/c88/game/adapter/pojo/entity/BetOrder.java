package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 會員注單紀錄
 *
 * @TableName ga_bet_order
 */
@Data
@TableName(value = "ga_bet_order")
public class BetOrder extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 注單ID
     */
    @TableField(value = "transaction_no")
    private String transactionNo;

    /**
     * 注單號
     */
    @TableField(value = "transaction_serial")
    private String transactionSerial;

    /**
     * 交易時間
     */
    @TableField(value = "transaction_time")
    private LocalDateTime transactionTime;

    /**
     * 會員ID
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 會員ID
     */
    @TableField(value = "username")
    private String username;

    /**
     * 三方平台ID
     */
    @TableField(value = "platform_id")
    private Long platformId;

    /**
     * 三方平台代碼
     */
    @TableField(value = "platform_code")
    private String platformCode;

    /**
     * 三方的遊戲ID
     */
    @TableField(value = "game_id")
    private String gameId;

    /**
     * 三方的遊戲名稱
     */
    @TableField(value = "game_name")
    private String gameName;

    /**
     * 遊戲類型
     */
    @TableField(value = "game_category_code")
    private String gameCategoryCode;

    /**
     * 該筆有效投注額
     */
    @TableField(value = "valid_bet_amount")
    private BigDecimal validBetAmount;

    /**
     * 該筆投注額
     */
    @TableField(value = "bet_amount")
    private BigDecimal betAmount;

    /**
     * 該筆總派彩
     */
    @TableField(value = "settle")
    private BigDecimal settle;

    /**
     * 派彩時間
     */
    @TableField(value = "settle_time")
    private LocalDateTime settleTime;

    /**
     * 該筆總輸贏
     */
    @TableField(value = "win_loss")
    private BigDecimal winLoss;

    /**
     * 該筆注單狀態 0:未派彩, 1:已派彩
     */
    @TableField(value = "bet_state")
    private Integer betState;

    /**
     * 反水計算狀態 0:未結算, 1:已結算
     */
    @TableField(value = "rebate_state")
    private Integer rebateState;

    /**
     * 已結算備註 (1已取消 2退款投注 3兌現)
     */
    @TableField(value = "settle_note")
    private Integer settleNote;

}