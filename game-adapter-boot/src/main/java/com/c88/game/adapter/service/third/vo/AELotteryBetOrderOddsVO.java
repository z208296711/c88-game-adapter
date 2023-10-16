package com.c88.game.adapter.service.third.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AELotteryBetOrderOddsVO {

    /**
     * 賠率代號
     */
    @JSONField(name = "key")
    private String key;

    /**
     * 賠率數值
     */
    @JSONField(name = "value")
    private BigDecimal value;

}
