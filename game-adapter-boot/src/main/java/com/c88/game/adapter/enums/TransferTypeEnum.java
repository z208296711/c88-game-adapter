package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum TransferTypeEnum implements IBaseEnum<Integer> {

    //目標為三方
    INTO_THIRD(1, "转出"),
    
    //轉入主錢包
    INTO_PLATFORM(0, "转入");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private String label;

    TransferTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    private static final Map<Integer, TransferTypeEnum> map = Stream.of(values()).collect(Collectors.toMap(TransferTypeEnum::getValue, Function.identity()));

    public static TransferTypeEnum fromIntValue(int value) {
        return map.get(value);
    }


}
