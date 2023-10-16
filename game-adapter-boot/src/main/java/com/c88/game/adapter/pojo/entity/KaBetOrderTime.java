package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * KA遊戲撈注單記錄列表
 */
@Data
@TableName(value ="ga_ka_bet_order_time")
public class KaBetOrderTime extends BaseEntity {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 開始時間
     */
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    private LocalDateTime endTime;

    /**
     * 抓取狀態 0:未抓 1:已抓 2:出錯
     */
    private Integer status;
}
