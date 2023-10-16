package com.c88.game.adapter.constants;

public class CQ9Constants {

    public static final String PLATFORM_CODE = "CQ9";

    /**
     * 交易状态 1:已结账
     */
    public static final String BET_SETTLED = "complete";

    /**
     * 成功狀態碼
     */
    public static final String RESPONSE_SUCCESS = "0";

    /**
     * 語系-英文
     */
    public static final String LANG_EN = "en";

    /**
     * 語系-越文
     */
    public static final String LANG_VI = "vn";

    /**
     * 語系-簡體中文
     */
    public static final String LANG_ZH_CN = "zh-cn";

    /**
     * 預設語系
     */
    public static final String LANG_DEFAULT = "vn";

    /**
     * 註冊
     */
    public static final String REGISTER_URL = "/gameboy/player";

    /**
     * 取Token
     */
    public static final String TOKEN_URL = "/gameboy/player/login";

    /**
     * 進遊戲
     */
    public static final String GAME_URL = "/gameboy/player/gamelink";

    /**
     * 轉出
     */
    public static final String TURN_OUT_URL = "/gameboy/player/withdraw";

    /**
     * 轉入
     */
    public static final String TURN_IN_URL = "/gameboy/player/deposit";

    /**
     * 交易狀態
     */
    public static final String TURN_STATUS_URL = "/gameboy/transaction/record/";

    /**
     * 查餘額
     */
    public static final String BALANCE_URL = "/gameboy/player/balance/";

    /**
     * 取得注單
     */
    public static final String BET_RECORD_URL = "/gameboy/order/view";

}
