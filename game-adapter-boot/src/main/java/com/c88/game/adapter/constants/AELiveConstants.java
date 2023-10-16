package com.c88.game.adapter.constants;

import java.util.List;

public class AELiveConstants {

    private AELiveConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PLATFORM_CODE = "AE_LIVE";

    /**
     * 測試營商主站ID
     */
    public static final String COMPANY_ID = "c88bettestvnd";

    /**
     * 測試Authorization
     */
    public static final String AUTHORIZATION_TOKEN = "6740905137b38dc4f1c34e5b275d05020bb9160f";

    public static final long AE_MIN = 10000;
    public static final long AE_MAX = 99999;

    /**
     * 交易状态 1:已结账, 2:注单无效
     */
    public static final Integer BET_SETTLED = 1;
    public static final Integer BET_CANCELED = 2;

    /**
     * 真人投注限制ID
     */
    public static final List<Integer> LIVE_BET_LIMIT_ID = List.of(261147, 261149, 261137, 261133, 261134, 261136);

    /**
     * 轉出部分額度
     */
    public static final String WITHDRAW_DEFAULT = "0";

    /**
     * 查詢限定用戶餘額
     */
    public static final String BALANCE_BY_USER = "0";

    /**
     * 查詢所有玩家餘額
     */
    public static final String BALANCE_BY_ALL = "1";

    /**
     * 平台ID
     */
    public static final String PLATFORM_LIVE_SEXYBCRT = "SEXYBCRT";
    public static final String PLATFORM_LIVE_VENUS = "VENUS";
    public static final String PLATFORM_LIVE_SV388 = "SV388";

    /**
     * 平台類型
     */
    public static final String GAME_TYPE = "LIVE";

    /**
     * 平台類型
     */
    public static final String GAME_CODE = "MX-LIVE-001";

    /**
     * 成功狀態碼
     */
    public static final String RESPONSE_SUCCESS = "0000";

    /**
     * 成功狀態碼
     */
    public static final String TRANSFER_NOT_FOUND = "1017";

    /**
     * 帳號已存在代碼
     */
    public static final String ACCOUNT_EXIST = "1001";

    /**
     * 預設幣別
     */
    public static final String DEFAULT_CURRENCY = "PTV";

    /**
     * 語系
     */
    public static final String LANG_EN = "en";
    public static final String LANG_CN = "cn";
    public static final String LANG_JP = "jp";
    public static final String LANG_TH = "th";
    public static final String LANG_VI = "vn";

    public static final String CURRENCY = "VND";

    /**
     * 註冊URL
     */
    public static final String REGISTER_URL = "/wallet/createMember";

    /**
     * 登入URL
     */
    public static final String LOGIN_URL = "/wallet/doLoginAndLaunchGame";

    /**
     * 轉出URL
     */
    public static final String TURN_OUT_URL = "/wallet/withdraw";

    /**
     * 轉入URL
     */
    public static final String TURN_IN_URL = "/wallet/deposit";

    /**
     * 交易狀態
     */
    public static final String TURN_STATUS_URL = "/wallet/checkTransferOperation";

    /**
     * 查餘額
     */
    public static final String BALANCE_URL = "/wallet/getBalance";

    /**
     * 取得注單URL
     */
    public static final String BET_RECORD_URL_BY_TIME = "/fetch/gzip/getTransactionByTxTime";
    public static final String BET_RECORD_URL_BY_TIME2 = "/fetch/getSummaryByTxTimeHour";
    public static final String BET_RECORD_UPDATE_URL_BY_TIME = "/fetch/getTransactionByUpdateDate";

    /**
     * 檢查注單
     */
    public static final String CHECK_RECORD_URL_BY_TIME = "/fetch/getSummaryByTxTimeHour";


}
