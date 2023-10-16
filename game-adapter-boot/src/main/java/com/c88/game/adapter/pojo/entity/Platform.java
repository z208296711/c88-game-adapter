package com.c88.game.adapter.pojo.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.mybatis.handler.JsonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 遊戲平台
 *
 * @TableName platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ga_platform", autoResultMap = true)
public class Platform extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 平台名稱
     */
    @TableField(value = "name")
    private String name;

    /**
     * 平台代碼
     */
    @TableField(value = "code")
    private String code;

    /**
     * 場館費(倍數)
     */
    @TableField(value = "rate")
    private BigDecimal rate;

    /**
     * 允許轉入 0否1可
     * 若開啟此選項，代表允許平台幣轉入此遊戲平台
     */
    @TableField(value = "can_transfer_in")
    private Integer canTransferIn;

    /**
     * 允許轉出 0否1可
     * 若開啟此選項，代表允許從此遊戲平台轉出至我們平台
     */
    @TableField(value = "can_transfer_out")
    private Integer canTransferOut;

    /**
     * 任務中心啟動ID
     */
    @TableField(value = "schedule_start_id")
    private Integer scheduleStartId;

    /**
     * 任務中心結束ID
     */
    @TableField(value = "schedule_end_id")
    private Integer scheduleEndId;

    /**
     * 維護 0沒有維護1維護中
     */
    @TableField(value = "maintain_state")
    private Integer maintainState;

    /**
     * 排程維護開關 0關閉 1開啟
     */
    @TableField(value = "schedule_maintain_state")
    private Integer scheduleMaintainState;

    /**
     * 排程維護時間類型 0無設定 1每日 2每週 3每月 4區間
     */
    @TableField(value = "schedule_type")
    private Integer scheduleType;

    /**
     * 排程 週
     */
    @TableField(value = "schedule_week")
    private Integer scheduleWeek;

    /**
     * 排程 月
     */
    @TableField(value = "schedule_month")
    private Integer scheduleMonth;

    /**
     * 排程維護開始時間
     */
    @TableField(value = "schedule_start_time")
    private LocalDateTime scheduleStartTime;

    /**
     * 排程維護關閉時間
     */
    @TableField(value = "schedule_end_time")
    private LocalDateTime scheduleEndTime;

    /**
     * 平台啟用狀態 0停用1啟用
     */
    @TableField(value = "enable")
    private Integer enable;

    /**
     * 是否被刪除 0否1是
     */
    @TableLogic
    @TableField(value = "delete_flag")
    private Integer deleteFlag;

    /**
     * API參數
     */
    @TableField(value = "api_parameter", typeHandler = JsonTypeHandler.class)
    private ApiParameter apiParameter;

    /**
     * 其他參數 for 遊戲大廳
     */
    @TableField(value = "lobby_code", typeHandler = FastjsonTypeHandler.class)
    private JSONObject lobbyCode;

}