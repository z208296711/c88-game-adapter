package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum TransferRecordTypeEnum implements IBaseEnum<Integer> {

    //主帳號轉出:0, 轉入主帳號:1
    ACCOUNT_TURN_OUT(0, "主帳號轉出"),

    ACCOUNT_TURN_IN(1, "轉入主帳號");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private String label;

    TransferRecordTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    private static final Map<Integer, TransferRecordTypeEnum> map = Stream.of(values()).collect(Collectors.toMap(TransferRecordTypeEnum::getValue, Function.identity()));

    public static TransferRecordTypeEnum fromIntValue(int value) {
        return map.get(value);
    }


}
