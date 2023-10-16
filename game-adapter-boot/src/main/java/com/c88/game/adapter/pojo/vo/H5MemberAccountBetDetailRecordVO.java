package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "帳務投注記錄")
public class H5MemberAccountBetDetailRecordVO {

    @Schema(title = "遊戲類型Code")
    private String gameCategoryCode;

    @Schema(title = "平台名稱")
    private String platformName;

    @Schema(title = "有效投注")
    private BigDecimal validBetAmount;

    @Schema(title = "輸贏")
    private BigDecimal winLoss;

    @Schema(title = "申請日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDate;

    @Schema(title = "平台遊戲詳情")
    private List<H5MemberAccountBetPlatformDetailRecordVO> platformDetail;

}
