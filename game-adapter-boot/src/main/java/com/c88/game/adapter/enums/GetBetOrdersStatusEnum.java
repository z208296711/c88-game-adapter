package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;


public enum GetBetOrdersStatusEnum implements IBaseEnum<Integer> {

    VERSION_STATUS_UNDONE(0, "未處理"),

    VERSION_STATUS_DONE(1, "已處理"),

    VERSION_STATUS_ERROR(2, "抓取失敗");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    GetBetOrdersStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
