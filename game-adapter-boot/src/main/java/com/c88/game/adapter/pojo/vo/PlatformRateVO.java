package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "查詢遊戲平台表單")
public class PlatformRateVO {

    @Schema(title = "平台ID")
    private Long id;

    @Schema(title = "平台名稱")
    private String name;

    @Schema(title = "費率", description = "倍數")
    private BigDecimal rate;

}
