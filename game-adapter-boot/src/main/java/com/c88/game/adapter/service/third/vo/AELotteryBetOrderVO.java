package com.c88.game.adapter.service.third.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class AELotteryBetOrderVO {

    /**
     * 清單
     */
    @JSONField(name = "list")
    private List<AELotteryBetOrderListVO> list;

    /**
     * 當前頁數
     */
    @JSONField(name = "page")
    private Integer page;

    /**
     * 最大頁數
     */
    @JSONField(name = "page_max")
    private Integer pageMax;

    /**
     * 總筆數
     */
    @JSONField(name = "total_record")
    private Integer totalRecord;

}
