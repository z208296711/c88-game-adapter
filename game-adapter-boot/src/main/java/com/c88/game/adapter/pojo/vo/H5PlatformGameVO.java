package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "取得平台遊戲")
public class H5PlatformGameVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "遊戲名稱", description = "越南語")
    private String nameVi;

    @Schema(title = "遊戲名稱", description = "英語")
    private String nameEn;

    @Schema(title = "平台ID")
    private String platformId;

    @Schema(title = "平台名稱")
    private String platformName;

    @Schema(title = "遊戲類型ID")
    private Integer gameCategoryId;

    @Schema(title = "遊戲類型名稱")
    private String gameCategoryName;

    @Schema(title = "PC裝置可見", description = "0不可見1可見")
    private Integer pcDeviceVisible;

    @Schema(title = "H5裝置可見", description = "0不可見1可見")
    private Integer h5DeviceVisible;

    @Schema(title = "平台排序")
    private Integer platformSort;

    @Schema(title = "遊戲排序")
    private Integer gameSort;

    @Schema(title = "遊戲圖片")
    private String gameImage;

    @Schema(title = "推薦圖片")
    private String recommendImage;

}
