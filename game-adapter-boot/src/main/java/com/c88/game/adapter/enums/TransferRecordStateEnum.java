package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum TransferRecordStateEnum implements IBaseEnum<Integer> {

    //狀態-> 開單:0, 處理中:1, 成功:2, 失敗:3, 掉單:4, 掉單轉成功:5, 掉單轉失敗:6
    OPEN(0, "開單"),

    IN_PROGRESS(1, "處理中"),

    SUCCESS(2, "成功"),

    FAIL(3, "失敗"),

    MISS(4, "掉單"),

    MISS_TO_SUCCESS(5, "掉單轉成功"),

    MISS_TO_FAIL(6, "掉單轉失敗");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private String label;

    TransferRecordStateEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    private static final Map<Integer, TransferRecordStateEnum> map = Stream.of(values()).collect(Collectors.toMap(TransferRecordStateEnum::getValue, Function.identity()));

    public static TransferRecordStateEnum fromIntValue(int value) {
        return map.get(value);
    }


}
