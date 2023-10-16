package com.c88.game.adapter.service.third.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CmdBetOrderVO {

    @JsonAlias("Id")
    private Long id;

    @JsonAlias("SourceName")
    private String sourceName;

    @JsonAlias("ReferenceNo")
    private String referenceNo;

    @JsonAlias("TransDate")
    private Long transDate;

    @JsonAlias("StateUpdateTs")
    private Long stateUpdateTs;

    @JsonAlias("isHomeGive")
    private Boolean isHomeGive;

    @JsonAlias("IsBetHome")
    private String isBetHome;

    @JsonAlias("BetAmount")
    private BigDecimal betAmount;

    @JsonAlias("Outstanding")
    private BigDecimal outstanding;

    @JsonAlias("Odds")
    private BigDecimal odds;

    @JsonAlias("Currency")
    private String currency;

    @JsonAlias("WinAmount")
    private BigDecimal winAmount;

    @JsonAlias("ExchangeRate")
    private BigDecimal exchangeRate;

    @JsonAlias("WinLoseStatus")
    private String winLoseStatus;

    @JsonAlias("SportType")
    private String sportType;

    @JsonAlias("TransType")
    private String transType;



    @JsonAlias("DangerStatus")
    private String dangerStatus;

    @JsonAlias("MemCommission")
    private BigDecimal memCommission;

    @JsonAlias("MatchID")
    private String matchId;

    @JsonAlias("IsCashOut")
    private Boolean isCashOut;

}
