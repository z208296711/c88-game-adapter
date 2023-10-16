package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.mybatis.handler.ListStringTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 平台遊戲列表
 *
 * @TableName platform_game
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ga_platform_game", autoResultMap = true)
public class PlatformGame extends BaseEntity {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 遊戲名稱-越南語
     */
    @TableField(value = "name_vi")
    private String nameVi;

    /**
     * 遊戲名稱-英語
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 平台ID
     */
    @TableField(value = "platform_id")
    private Long platformId;

    /**
     * 平台名稱
     */
    @TableField(value = "platform_name")
    private String platformName;

    /**
     * 遊戲類型ID
     */
    @TableField(value = "game_category_id")
    private Integer gameCategoryId;

    /**
     * 遊戲類型名稱
     */
    @TableField(value = "game_category_name")
    private String gameCategoryName;

    /**
     * 遊戲ID
     */
    @TableField(value = "game_id")
    private String gameId;

    /**
     * 額外欄位
     */
    @TableField(value = "extend_field")
    private String extendField;

    /**
     * PC裝置可見 0不可見1可見
     */
    @TableField(value = "pc_device_visible")
    private Integer pcDeviceVisible;

    /**
     * H5裝置可見 0不可見1可見
     */
    @TableField(value = "h5_device_visible")
    private Integer h5DeviceVisible;

    /**
     * PC代碼1
     */
    @TableField(value = "pc_code1")
    private String pcCode1;

    /**
     * PC代碼2
     */
    @TableField(value = "pc_code2")
    private String pcCode2;

    /**
     * H5代碼1
     */
    @TableField(value = "h5_code1")
    private String h5Code1;

    /**
     * H5代碼2
     */
    @TableField(value = "h5_code2")
    private String h5Code2;

    /**
     * 標籤組
     */
    @TableField(value = "tags", typeHandler = ListStringTypeHandler.class)
    private List<String> tags;

    /**
     * 遊戲排序
     */
    @TableField(value = "game_sort")
    private Integer gameSort;

    /**
     * 熱門遊戲 0否1是
     */
    @TableField(value = "hot_flag")
    private Integer hotFlag;

    /**
     * 推薦遊戲 0否1是
     */
    @TableField(value = "recommend_flag")
    private Integer recommendFlag;

    /**
     * 啟用狀態 0停用1啟用
     */
    @TableField(value = "enable")
    private Integer enable;

    /**
     * 遊戲圖片
     */
    @TableField(value = "game_image")
    private String gameImage;

    /**
     * 推薦圖片
     */
    @TableField(value = "recommend_image")
    private String recommendImage;

}