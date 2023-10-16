package com.c88.game.adapter.service.third.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PGBetOrderVO {

    @ApiModelProperty("會員帳號")
    private String playerName;
    @ApiModelProperty("注單ID")
    private String betId;
    @ApiModelProperty("遊戲ID")
    private String gameId;
    @ApiModelProperty("投注時間")
    private Long betTime;
    @ApiModelProperty("投注金額")
    private BigDecimal betAmount;
    @ApiModelProperty("派彩時間")
    private Long betEndTime;
    @ApiModelProperty("派彩金額")
    private BigDecimal winAmount;
}
