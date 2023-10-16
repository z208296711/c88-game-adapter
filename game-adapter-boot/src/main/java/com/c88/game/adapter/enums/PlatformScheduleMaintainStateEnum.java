package com.c88.game.adapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 例行維護開關
 */
@Getter
@AllArgsConstructor
public enum PlatformScheduleMaintainStateEnum {

    MAINTAIN_STATE_END(0, "例行維護停用"),
    MAINTAIN_STATE_START(1, "例行維護啟用");

    private final Integer code;

    private final String label;

    public static PlatformScheduleMaintainStateEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}
