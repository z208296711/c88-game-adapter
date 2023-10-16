package com.c88.game.adapter.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TotalCountVO {
    String category;
    BigDecimal totalBetAmount;
    BigDecimal totalValidBetAmount;
}
