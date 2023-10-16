package com.c88.game.adapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum MemberAccountTransferRecordStatusEnum {

    ALL(0, "全部"),
    SUCCESS(1, "成功"),
    FAIL(2, "失敗");

    private final Integer code;

    private final String label;

    public static MemberAccountTransferRecordStatusEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }
}
