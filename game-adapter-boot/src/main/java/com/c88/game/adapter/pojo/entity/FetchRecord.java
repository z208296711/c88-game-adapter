package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 三方遊戲Pull紀錄
 * @TableName ga_fetch_record
 */
@TableName(value ="ga_fetch_record")
@Data
public class FetchRecord implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 遊戲簡稱
     */
    @TableField(value = "platform_code")
    private String platformCode;

    /**
     * 抓取開始時間
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 抓取結束時間
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 抓取狀態 0:未抓 1:已抓 2:出錯
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 更新時間
     */
    @TableField(value = "gmt_modified")
    private LocalDateTime gmtModified;

    /**
     * 創建時間
     */
    @TableField(value = "gmt_create")
    private LocalDateTime gmtCreate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}