package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 
 * @TableName ga_transfer_order
 */
@TableName(value ="ga_transfer_order")
@Data
public class TransferOrder extends BaseEntity {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 會員id
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 會員帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 所屬平台
     */
    @TableField(value = "platform_code")
    private String platformCode;

    /**
     * 主帳號轉出:0, 轉入主帳號:1
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 轉帳單號
     */
    @TableField(value = "serial_no")
    private String serialNo;

    /**
     * 轉帳前餘額
     */
    @TableField(value = "before_balance")
    private BigDecimal beforeBalance;

    /**
     * 轉帳金額
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 轉帳後餘額
     */
    @TableField(value = "after_balance")
    private BigDecimal afterBalance;

    /**
     * 狀態-> 開單:0, 處理中:1, 成功:2, 失敗:3, 掉單:4, 掉單轉成功:5, 掉單轉失敗:6
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 處理時間
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 處理人員
     */
    @TableField(value = "update_by")
    private String updateBy;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}