package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AE彩票遊戲撈注單記錄列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ga_ae_lottery_bet_order_time")
public class AeLotteryBetOrderTime extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 開始時間
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 抓取狀態 0:未抓 1:已抓 2:出錯
     */
    @TableField(value = "status")
    private Integer status;
}
