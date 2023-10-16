package com.c88.game.adapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum PlatformGameLabelEnum {

    ALL(0, "全部"),
    HOT(1, "熱門"),
    RECENT(2, "近期"),
    RECOMMEND_BANNER(3, "推薦遊戲的Banner");

    private final Integer code;

    private final String label;

    public static PlatformGameLabelEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}
