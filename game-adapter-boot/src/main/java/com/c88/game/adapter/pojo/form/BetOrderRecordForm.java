package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(title = "注單記錄")
public class BetOrderRecordForm extends BasePageQuery {
    @Schema(title = "會員id")
    private Long memberId;

    @Schema(title = "開始時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String startTime;

    @Schema(title = "結束時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String endTime;

    @Schema(title = "遊戲平台")
    Integer platformId;

    @Schema(title = "遊戲類型")
    Integer category;

    @Schema(title = "金額下限")
    BigDecimal minBet;

    @Schema(title = "金額上限")
    BigDecimal maxBet;
}
