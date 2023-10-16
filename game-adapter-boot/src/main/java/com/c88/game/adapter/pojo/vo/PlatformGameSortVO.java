package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "查詢平台遊戲排序內容")
public class PlatformGameSortVO {

    @Schema(title = "id")
    private Integer id;

    @Schema(title = "遊戲名稱", description = "越南語")
    private String nameVi;

    @Schema(title = "遊戲名稱", description = "英語")
    private String nameEn;

    @Schema(title = "排序")
    private Integer gameSort;

}
