package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(title = "找遊戲輸贏報表表單")
public class FindMemberGameWinLossForm {

    @NotNull(message = "開始時間不得為空")
    @Schema(title = "開始時間")
    @Parameter(description = "開始時間")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "結束時間不得為空")
    @Schema(title = "結束時間")
    @Parameter(description = "結束時間")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "時間類型不得為空")
    @Parameter(description = "時間類型 1投注時間 2結算時間")
    @Schema(title = "時間類型", description = "1投注時間 2結算時間")
    private Integer timeType;

}
