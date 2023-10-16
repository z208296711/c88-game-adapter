package com.c88.game.adapter.constants;

public class RedisKey {

    private RedisKey() {
        throw new IllegalStateException("Utility class");
    }

    public static final String TOKEN_VALID_BY_CMD = "tokenValidByCMD";

    public static final String TOKEN_VALID_BY_KA = "tokenValidByKA";

    public static final String TOKEN_VALID_BY_SBA = "tokenValidBySBA";
    public static final String TOKEN_VALID_BY_PS = "tokenValidByPS";

    public static final String PLATFORM_BY_ID = "platformById";

    public static final String PLATFORM_GAME_BY_ID = "platformGameById";

    public static final String PLATFORM_GAME_BY_GAME_ID = "platformGameByGameId";

    public static final String PLATFORM_GAME_MEMBER = "platformGameMember";

    public static final String GAME_CATEGORY_PLATFORM = "gameCategoryPlatform";

    public static final String PLATFORM_GAME_LABEL = "platformGameLabel";

    public static final String PLATFORM_GAME = "platformGame";

    public static final String TOKEN_VALID_BY_DS88 = "tokenValidByDS88";
    public static final String MEMBER_BALANCE = "memberBalance";

    public static final String GAME_TRANSFER_TO_PLATFORM = "gameTransferToPlatform";

}
