package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(title = "查詢遊戲平台表單")
public class PlatformVO {

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "平台名稱")
    private String name;

    @Schema(title = "平台代碼")
    private String code;

    @Schema(title = "允許轉入", description = "0否1可")
    private Integer canTransferIn;

    @Schema(title = "允許轉出", description = "0否1可")
    private Integer canTransferOut;

    @Schema(title = "維護狀態", description = "0沒有維護1維護中")
    private Integer maintainState;

    @Schema(title = "排程維護開關", description = "0關閉 1開啟")
    private Integer scheduleMaintainState;

    @Schema(title = "排程維護時間類型", description = "0無設定 1每日 2每週 3每月 4區間")
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

    @Schema(title = "平台啟用狀態", description = "0停用1啟用")
    private Integer enable;

    @Schema(title = "熱門遊戲選單")
    private List<PlatformHotGameVO> platformHotGames;

}
