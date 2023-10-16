package com.c88.game.adapter.pojo.entity;

import lombok.Data;

@Data
public class ApiParameter {

    /**
     * 主要呼叫的URL
     */
    private String apiUrl;

    /**
     * 進遊戲的的URL
     */
    private String gameUrl;

    /**
     * 拉注單的URL
     */
    private String recordUrl;

    /**
     * 代理ID,商戶號之類的東西
     */
    private String apiId;

    /**
     * 代理密鑰,商戶密鑰之類的東西
     */
    private String apiKey;

    /**
     * 密鑰
     */
    private String secretKey;

    /**
     * 三方要求一定要帶的帳號前綴
     */
    private String prefix;

    /**
     * 帳號後綴
     */
    private String suffix;

    /**
     * 語系
     */
    private String locale;

    /**
     * 三方各自需要的參數
     */
    private String others;
}
