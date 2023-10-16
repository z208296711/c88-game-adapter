package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 遊戲類型
 *
 * @TableName game_template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ga_game_category")
public class GameCategory extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 平台名稱
     */
    @TableField(value = "name")
    private String name;

    /**
     * 代碼
     */
    @TableField(value = "code")
    private String code;

    /**
     * 備註
     */
    @TableField(value = "note")
    private String note;

    /**
     * 是否直接轉入遊戲大廳 0否1是
     */
    @TableField(value = "to_game_lobby")
    private Integer toGameLobby;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 是否被刪除 0否1是
     */
    @TableLogic
    @TableField(value = "delete_flag")
    private Integer deleteFlag;

    /**
     * WEB遊戲圖片
     */
    @TableField(value = "web_image")
    private String webImage;

    /**
     * H5遊戲圖片
     */
    @TableField(value = "h5_image")
    private String h5Image;

}