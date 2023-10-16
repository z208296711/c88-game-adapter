package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "每日平台投注量")
public class MemberBetAmountRecordDailyVO {

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "會員帳號")
    private String username;

    @Schema(title = "平台代碼")
    private String platformCode;

    @Schema(title = "平台ID")
    private Integer platformId;

    @Schema(title = "遊戲類型代碼")
    private String gameCategoryCode;

    @Schema(title = "該筆投注額")
    private BigDecimal betAmount;

    @Schema(title = "該筆有效投注額")
    private BigDecimal validBetAmount;

    @Schema(title = "該筆總派彩")
    private BigDecimal settle;

    @Schema(title = "該筆總輸贏")
    private BigDecimal winLoss;

    @Schema(title = "返水狀態")
    private BigDecimal rebateStatus;

    @Schema(title = "派彩時間")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settleTime;

    @Schema(title = "bet_times")
    private Integer betTimes;

    @Schema(title = "VIP ID")
    private Integer vipId;

    @Schema(title = "VIP名稱")
    private String vipName;

    @Schema(title = "創建日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime gmtCreate;

}
