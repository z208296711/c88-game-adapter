package com.c88.game.adapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 排序方式
 */
@Getter
@AllArgsConstructor
public enum SortTypeEnum {

    TOP(0, "置頂"),
    BOTTOM(1, "置底");

    private final Integer code;

    private final String note;

    public static SortTypeEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}
