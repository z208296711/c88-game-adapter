package com.c88.game.adapter.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2022/12/21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PPGameBetInfoVO {
    private String playerID;

    private String extPlayerID;

    private String gameID;

    private String playSessionID;

    private String parentSessionID;

    private String startDate;

    private String endDate;

    private String status;

    private String type;

    private BigDecimal bet;

    private BigDecimal win;

    private String currency;

    private BigDecimal jackpot;
}
