package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


@Data
@Schema(title = "返水記錄")
public class BetRebateRecordForm extends BasePageQuery {
    @Parameter(description = "帳號")
    @Schema(title = "帳號")
    private String username;

    @Parameter(description = "開始時間")
    @Schema(title = "開始時間")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Parameter(description = "結束時間")
    @Schema(title = "結束時間")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
