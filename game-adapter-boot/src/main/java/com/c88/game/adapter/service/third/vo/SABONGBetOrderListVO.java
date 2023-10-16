package com.c88.game.adapter.service.third.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SABONGBetOrderListVO {

    /**
     * 投注創建時間
     */
    private String createdDate;

    /**
     * 投注修改時間
     */
    private String modifiedDate;

    /**
     * 投注記錄唯一標示
     */
    private String transactionId;

    /**
     * 比賽的唯一標示
     */
    private String matchCode;

    /**
     * 會員遊戲帳號
     */
    private String playerNumber;

    /**
     * 商戶唯一標示
     */
    private String operatorCode;

    /**
     * 投注金額
     */
    private BigDecimal betAmount;

    /**
     * Meron or Wala 投注
     */
    private String betCorner;

    /**
     * 總輸贏
     */
    private BigDecimal winAmount;

    /**
     * 回傳輸贏的結果
     */
    private String betResult;

    /**
     * 下注使用貨幣
     */
    private String currency;

    /**
     * 商戶品牌
     */
    private String brandCode;

    /**
     * 投注結果是否被逆轉
     */
    private String reversed;

    /**
     * 創建投注時間
     */
    private String transactionCreatedDate;

    /**
     * 創建修改時間
     */
    private String transactionModifiedDate;

}
