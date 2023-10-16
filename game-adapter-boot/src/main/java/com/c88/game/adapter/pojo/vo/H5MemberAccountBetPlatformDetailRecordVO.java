package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "帳務投注平台遊戲記錄詳情")
public class H5MemberAccountBetPlatformDetailRecordVO {

    @Schema(title = "遊戲名稱")
    private String gameName;

    @Schema(title = "投注金額")
    private BigDecimal betAmount;

    @Schema(title = "有效投注")
    private BigDecimal validBetAmount;

    @Schema(title = "輸贏")
    private BigDecimal winLoss;

    @Schema(title = "單號")
    private String transactionSerial;

    @Schema(title = "狀態", description = "0未派彩 1已派彩")
    private Integer betState;

    @Schema(title = "反水計算狀態", description = "0未結算 1已結算")
    private Integer rebateState;

    @Schema(title = "已結算備註", description = "1已取消 2退款投注 3兌現")
    private Integer note;

    @Schema(title = "申請時間")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime createTime;

}
