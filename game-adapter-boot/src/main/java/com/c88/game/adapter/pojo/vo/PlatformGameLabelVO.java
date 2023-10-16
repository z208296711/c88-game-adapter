package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "取得全部遊戲列表")
public class PlatformGameLabelVO {

    @Schema(title = "id")
    private Integer id;

    @Schema(title = "遊戲名稱", description = "越南語")
    private String nameVi;

    @Schema(title = "遊戲名稱", description = "英語")
    private String nameEn;

    @Schema(title = "平台ID")
    private Integer platformId;

    @Schema(title = "平台名稱")
    private String platformName;

    @Schema(title = "遊戲類型ID")
    private Integer gameCategoryId;

    @Schema(title = "遊戲類型名稱")
    private String gameCategoryName;

    @Schema(title = "遊戲ID")
    private String gameId;

    @Schema(title = "遊戲圖片")
    private String gameImage;

    @Schema(title = "推薦圖片")
    private String recommendImage;
}
