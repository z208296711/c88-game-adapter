package com.c88.game.adapter.service.third.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvoBetOrderVO {

    @ApiModelProperty("交易資訊")
    private List<JSONObject> participants;

    @ApiModelProperty("遊戲資訊")
    private JSONObject table;

    @ApiModelProperty("該筆有效投注額")
    private BigDecimal wager;

    @ApiModelProperty("該筆總派彩")
    private BigDecimal payout;

    @ApiModelProperty("派彩時間")
    private LocalDateTime settledAt;

}
