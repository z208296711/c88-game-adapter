package com.c88.game.adapter.pojo.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(title = "修改遊戲平台")
public class ModifyPlatformForm {

    @NotNull(message = "ID不得為空")
    @Schema(title = "ID")
    private Long id;

    @Schema(title = "平台名稱")
    private String name;

    @Range(min = 0, max = 1, message = "只能輸入0-1")
    @Schema(title = "允許轉入", description = "0否1可")
    private Integer canTransferIn;

    @Range(min = 0, max = 1, message = "只能輸入0-1")
    @Schema(title = "允許轉出", description = "0停用1啟用")
    private Integer canTransferOut;

    @Range(min = 0, max = 1, message = "只能輸入0-1")
    @Schema(title = "維護", description = "0沒有維護1維護中")
    private Integer maintainState;

    @Range(min = 0, max = 1, message = "只能輸入0-1")
    @Schema(title = "排程維護開關", description = "0關閉 1開啟")
    private Integer scheduleMaintainState;

    @Range(min = 0, max = 4, message = "只能輸入0-4")
    @Schema(title = "程維護時間類型", description = "0無設定 1每日 2每週 3每月 4區間")
    private Integer scheduleType;

    @Schema(title = "排程 週")
    private Integer scheduleWeek;

    @Schema(title = "排程 月")
    private Integer scheduleMonth;

    @Schema(title = "排程維護開始時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleStartTime;

    @Schema(title = "排程維護關閉時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleEndTime;

    @Range(min = 0, max = 1, message = "只能輸入0-1")
    @Schema(title = "平台啟用狀態", description = "0停用1啟用")
    private Integer enable;

    @Schema(title = "平台圖片")
    private String image;

    @Schema(title = "平台選擇圖片")
    private String selectImage;

    @Schema(title = "排序")
    private Integer sort;

}
