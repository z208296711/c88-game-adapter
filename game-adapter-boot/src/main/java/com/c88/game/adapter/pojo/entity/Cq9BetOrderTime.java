package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

/**
 * CQ9遊戲撈注單記錄列表
 * @TableName ga_cq9_bet_order_time
 */
@Data
@TableName(value ="ga_cq9_bet_order_time")
public class Cq9BetOrderTime extends BaseEntity {
    /**
     * Id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 開始時間
     */
    private Long startTime;

    /**
     * 結束時間
     */
    private Long endTime;

    /**
     * 抓取狀態 0:未抓 1:已抓 2:出錯
     */
    private Integer status;
}
