package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(title = "查詢遊戲平台表單")
public class PlatformGameVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "遊戲名稱", description = "越南語")
    private String nameVi;

    @Schema(title = "遊戲名稱", description = "英語")
    private String nameEn;

    @Schema(title = "平台ID")
    private Long platformId;

    @Schema(title = "平台名稱")
    private String platformName;

    @Schema(title = "遊戲類型ID")
    private Integer gameCategoryId;

    @Schema(title = "遊戲類型名稱")
    private String gameCategoryName;

    @Schema(title = "遊戲ID")
    private String gameId;

    @Schema(title = "額外欄位")
    private String extendField;

    @Schema(title = "PC裝置可見", description = "0不可見1可見")
    private Integer pcDeviceVisible;

    @Schema(title = "H5裝置可見", description = "0不可見1可見")
    private Integer h5DeviceVisible;

    @Schema(title = "熱門遊戲", description = "0否1是")
    private Integer hotFlag;

    @Schema(title = "推薦遊戲", description = "0否1是")
    private Integer recommendFlag;

    @Schema(title = "標籤")
    private String tags;

    @Schema(title = "PC代碼1")
    private String pcCode1;

    @Schema(title = "PC代碼2")
    private String pcCode2;

    @Schema(title = "H5代碼1")
    private String h5Code1;

    @Schema(title = "H5代碼2")
    private String h5Code2;

    @Schema(title = "遊戲圖片")
    private String gameImage;

    @Schema(title = "推薦圖片")
    private String recommendImage;

    @Schema(title = "啟用狀態", description = "0停用1啟用")
    private Integer enable;

    @Schema(title = "新增時間 0停用1啟用")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;

}
