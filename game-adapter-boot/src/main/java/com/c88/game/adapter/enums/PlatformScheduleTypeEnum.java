package com.c88.game.adapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 例行維護類型
 */
@Getter
@AllArgsConstructor
public enum PlatformScheduleTypeEnum {

    NOT(0, "無設定"),
    DAY(1, "每日"),
    WEEK(2, "每週"),
    MONTH(3, "每月"),
    SCOPE(4, "區間");

    private final Integer code;

    private final String label;

    public static PlatformScheduleTypeEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}
