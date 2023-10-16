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

/**
 * 遊戲類型-平台排序
 *
 * @TableName ga_platform_sort
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ga_platform_sort")
public class PlatformSort extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 遊戲類型ID
     */
    private Integer gameCategoryId;

    /**
     * 平台ID
     */
    private Integer platformId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 熱門遊戲 0否 1是
     */
    @TableField(value = "hot")
    private Integer hot;

    /**
     * 熱門遊戲排序
     */
    @TableField(value = "hot_sort")
    private Integer hotSort;

    /**
     * 平台圖片
     */
    private String image;

    /**
     * 平台選擇圖片
     */
    private String selectImage;

}