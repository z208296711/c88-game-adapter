package com.c88.game.adapter.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "遊戲列表樣式")
public class PlatformGameStyleVO {

    /**
     * 遊戲類型CODE HOT為熱門遊戲不屬於任何類型
     */
    @TableField(value = "game_category_code")
    private String  gameCategoryCode;

    /**
     * 樣式
     */
    @TableField(value = "style_type")
    private Integer styleType;

}
