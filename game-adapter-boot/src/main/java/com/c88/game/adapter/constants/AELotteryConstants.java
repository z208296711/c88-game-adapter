package com.c88.game.adapter.constants;

import java.util.List;

public class AELotteryConstants {

    private AELotteryConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PLATFORM_CODE = "AE_LOTTERY";

    public static final String GAME_CATEGORY_CODE = "lottery";

    /**
     * 測試營商主站ID
     */
    public static final String COMPANY_ID = "c8bet";

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
     * 轉出部分額度
     */
    public static final long WITHDRAW_DEFAULT = 0;

    /**
     * 查詢限定用戶餘額
     */
    public static final long BALANCE_ALL = 0;

    /**
     * 平台ID
     */
    public static final String PLATFORM = "SEXYBCRT";


    /**
     * 平台類型
     */
    public static final String GAME_CODE = "MX-LIVE-001";

    /**
     * 成功狀態碼
     */
    public static final String RESPONSE_SUCCESS = "0";

    /**
     * 成功狀態碼
     */
    public static final String TRANSFER_NOT_FOUND = "1017";

    /**
     * 帳號存在錯誤碼
     */
    public static final String USERNAME_EXISTS = "206";

    /**
     * 預設幣別
     */
    public static final String DEFAULT_CURRENCY = "PTV";

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
    public static final String LANG_ZH_CN = "cn";

    /**
     * 預設語系
     */
    public static final String LANG_DEFAULT = "vn";

    /**
     * 語系對應表
     */
    public static final List<String> LANG_MAP = List.of("zh_cn", "vi", "en");

    /**
     * 註冊URL
     */
    public static final String REGISTER_URL = "/api/v1/player/create-user";

    /**
     * 登入URL
     */
    public static final String LOGIN_URL = "/api/v1/player/game-url";

    /**
     * 轉出URL
     */
    public static final String TURN_OUT_URL = "/api/v1/wallet/transfer";

    /**
     * 轉入URL
     */
    public static final String TURN_IN_URL = "/api/v1/wallet/transfer";

    /**
     * 交易狀態
     */
    public static final String TURN_STATUS_URL = "/api/v1/wallet/transfer-transaction-id-detail";

    /**
     * 查餘額
     */
    public static final String BALANCE_URL = "/api/v1/player/balance";

    /**
     * 取得注單URL
     */
    public static final String BET_RECORD_URL_BY_TIME = "/api/v1/order/bet-record";

    public static final String BET_RECORD_UPDATE_URL_BY_TIME = "/fetch/getTransactionByUpdateDate";

    /**
     * 檢查注單
     */
    public static final String CHECK_RECORD_URL_BY_TIME = "/api/v1/order/detail-url";


}
