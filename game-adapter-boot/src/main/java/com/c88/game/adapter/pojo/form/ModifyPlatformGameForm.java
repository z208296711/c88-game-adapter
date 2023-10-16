package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改平台遊戲表單")
public class ModifyPlatformGameForm {

    @NotNull(message = "平台遊戲ID不得為空")
    @Schema(title = "平台遊戲ID")
    private Integer id;

    @Schema(title = "遊戲名稱", description = "越南語")
    private String nameVi;

    @Schema(title = "遊戲名稱", description = "英文")
    private String nameEn;

    @Schema(title = "平台ID")
    private Long platformId;

    @Schema(title = "遊戲類型ID")
    private Integer gameCategoryId;

    @Range(min = 0, max = 1, message = "請輸入0~1數字")
    @Schema(title = "PC裝置可見", description = "0不可見1可見")
    private Integer pcDeviceVisible;

    @Range(min = 0, max = 1, message = "請輸入0~1數字")
    @Schema(title = "H5裝置可見", description = "0不可見1可見")
    private Integer h5DeviceVisible;

    @Schema(title = "遊戲ID")
    private String gameId;

    @Schema(title = "額外欄位")
    private String extendField;

    @Schema(title = "標籤組")
    private String tags;

    @Schema(title = "熱門遊戲註記", description = "0否1是")
    private Integer hotFlag;

    @Schema(title = "推薦遊戲註記", description = "0否1是")
    private Integer recommendFlag;

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

    @Schema(title = "啟用")
    private Integer enable;
}
