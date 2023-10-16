package com.c88.game.adapter.service.third.png;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PNGBetVO {

    @JSONField(name="TransactionId")
    private String transactionId;

    @JSONField(name="TransactionId")
    private String time;

    @JSONField(name="ProductGroup")
    private String productGroup;

    @JSONField(name="ExternalUserId")
    private String externalUserId;

    @JSONField(name="GamesessionId")
    private String gamesessionId;

    @JSONField(name="GameId")
    private String gameId;

    @JSONField(name="Currency")
    private String currency;

    @JSONField(name="Balance")
    private BigDecimal balance;

    @JSONField(name="TotalLoss")
    private BigDecimal totalLoss;

    @JSONField(name="TotalGain")
    private BigDecimal totalGain;

    @JSONField(name="GamesessionStarted")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime gameSessionStarted;

    @JSONField(name="GamesessionFinished")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime gameSessionFinished;

    @JSONField(name="exchangeRate")
    private BigDecimal ExchangeRate;

    @JSONField(name="MessageType")
    private Integer messageType;

    @JSONField(name="MessageTimestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime messageTimestamp;


}
