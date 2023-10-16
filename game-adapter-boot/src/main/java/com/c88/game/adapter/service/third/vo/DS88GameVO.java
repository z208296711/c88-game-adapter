package com.c88.game.adapter.service.third.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DS88GameVO {
    String slug;
    String arenaFightNo;
    String flightNo;
    Long roundId;
    String side;
    String account;
    String status;
    BigDecimal odd;
    BigDecimal betAmount;
    BigDecimal netIncome;
    BigDecimal betReturn;
    BigDecimal validAmount;
    String result;
    Boolean isSettled;
    LocalDateTime betAt;
    LocalDateTime settledAt;
}
