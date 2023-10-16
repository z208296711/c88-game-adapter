package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.c88.common.core.base.BaseEntity;
import lombok.Data;

/**
 * 遊戲列表樣式
 * @TableName ga_platform_game_style
 */
@TableName(value ="ga_platform_game_style")
@Data
public class PlatformGameStyle extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 遊戲類型CODE HOT為熱門遊戲不屬於任何類型
     */
    @TableField(value = "game_category_code")
    private String gameCategoryCode;

    /**
     * 樣式
     */
    @TableField(value = "style_type")
    private Integer styleType;

}