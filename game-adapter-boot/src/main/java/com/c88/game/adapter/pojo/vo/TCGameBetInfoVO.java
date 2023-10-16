package com.c88.game.adapter.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TCGameBetInfoVO {

    private Long orderMasterId;

    private String numero;

    private String orderNum;

    private String userName;

    private String gameCode;

    private String gameGroupName;

    private BigDecimal betAmount;

    private BigDecimal actualBetAmount;


    private BigDecimal winAmount;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime drawTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bettingTime;

}