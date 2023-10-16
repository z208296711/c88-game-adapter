package com.c88.game.adapter.service.third.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class AELotteryBetOrderInfoVO {

    /**
     * 賠率
     */
    @JSONField(name = "odds")
    private AELotteryBetOrderOddsVO odds;

    /**
     * 下注位置
     * ( 例如:組選時 ["1","2"]
     */
    @JSONField(name = "items")
    private List<String> items;

}
