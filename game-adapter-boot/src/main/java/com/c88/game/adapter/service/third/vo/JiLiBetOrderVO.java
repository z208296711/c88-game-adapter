package com.c88.game.adapter.service.third.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class  JiLiBetOrderVO {

    @ApiModelProperty("會員帳號")
    @JsonAlias("Account")
    private String account;
    @ApiModelProperty("注單ID")
    @JsonAlias("WagersId")
    private String wagersId;
    @ApiModelProperty("遊戲ID")
    @JsonAlias("GameId")
    private String gameId;
    @ApiModelProperty("投注時間")
    @JsonAlias("WagersTime")
    private LocalDateTime wagersTime;
    @ApiModelProperty("投注金額")
    @JsonAlias("BetAmount")
    private BigDecimal betAmount;
    @ApiModelProperty("派彩時間")
    @JsonAlias("PayoffTime")
    private LocalDateTime payoffTime;
    @ApiModelProperty("派彩金額")
    @JsonAlias("PayoffAmount")
    private BigDecimal payoffAmount;
}
