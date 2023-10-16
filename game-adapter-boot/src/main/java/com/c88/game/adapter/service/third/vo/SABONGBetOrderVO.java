package com.c88.game.adapter.service.third.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class SABONGBetOrderVO {

    /**
     * 清單
     */
    @JSONField(name = "data")
    private List<SABONGBetOrderListVO> list;

    /**
     * 總比數
     */
    @JSONField(name = "recordCount")
    private Integer recordCount;

    /**
     * 當前頁數
     */
    @JSONField(name = "pageNumber")
    private Integer pageNumber;

    /**
     * 一頁筆數
     */
    @JSONField(name = "pageSize")
    private Integer pageSize;

    /**
     * 總頁數
     */
    @JSONField(name = "pageCount")
    private Integer pageCount;

}
