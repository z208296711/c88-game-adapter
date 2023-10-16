package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;


public enum ReBateStatusEnum implements IBaseEnum<Integer> {

    UN_SETTLED(0, "未結算"),
    SETTLED(1, "已結算"),
    CANCEL(2, "已取消"),
    CASH(3, "兌現");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private String label;

    ReBateStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
