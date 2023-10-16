package com.c88.game.adapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum PlatformMaintainStateEnum {

    MAINTAIN_END(0, "維護結束"),
    MAINTAIN_START(1, "維護中");

    private final Integer code;

    private final String label;

    public static PlatformMaintainStateEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }
}
