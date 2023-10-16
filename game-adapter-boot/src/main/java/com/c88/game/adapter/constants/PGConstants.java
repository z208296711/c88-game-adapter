package com.c88.game.adapter.constants;

public class PGConstants {

    public static final String PLATFORM_CODE = "PG";

    /**
     * 預設貨幣
     */
    public static final String CURRENCY_DEFAULT = "PHP";

    /**
     * 成功狀態碼
     */
    public static final String RESPONSE_SUCCESS = "0";

    /**
     * 註冊
     */
    public static final String REGISTER_URL = "/Player/v1/Create";

    /**
     * 登入
     */
    public static final String LOGIN_URL = "/Login/v1/LoginGame";

    /**
     * 轉入
     */
    public static final String TURN_IN_URL = "/Cash/v3/TransferIn";

    /**
     * 轉出
     */
    public static final String TURN_OUT_URL = "/Cash/v3/TransferOut";

    /**
     * 交易狀態
     */
    public static final String TURN_STATUS_URL = "/Cash/v3/GetSingleTransaction";

    /**
     * 查餘額
     */
    public static final String BALANCE_URL = "/Cash/v3/GetPlayerWallet";

    /**
     * 取得注單
     */
    public static final String BET_RECORD_URL = "/Bet/v4/GetHistoryForSpecificTimeRange";

}
