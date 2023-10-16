package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "資本概括 中心錢包")
public class CapitalSummaryCenterVO {

    @Schema(title = "平台名稱")
    private String platformName;

    @Schema(title = "平台代碼")
    private String platformCode;

    @Schema(title = "餘額")
    private BigDecimal balance;

}
