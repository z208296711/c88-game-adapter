package com.c88.game.adapter.service.third.vo;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AELiveBetOrderListVO {


    /**
     * 狀態代碼
     */
    @JSONField(name = "status")
    private String status;

    /**
     * 下注內容
     */
    @JSONField(name = "transactions")
    private List<AELiveTransactionVO> transactions;

}
