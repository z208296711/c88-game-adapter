package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;


public enum BetOrderTypeEnum implements IBaseEnum<Integer> {

    BET_STATUS_CONFIRMED(0, "未派彩"),
    BET_STATUS_SETTLED(1, "已派彩"),
    BET_STATUS_CANCELED(-1, "已取消");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private String label;

    BetOrderTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
