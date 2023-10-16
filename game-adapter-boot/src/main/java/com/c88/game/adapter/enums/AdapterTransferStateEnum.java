package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;


public enum AdapterTransferStateEnum implements IBaseEnum<Integer> {

    //狀態-> 成功:1 失敗:-1 未確認:0


    SUCCESS(1, "成功"),
    FAIL(2, "失敗"),
    IN_PROGRESS(3,""),
    UNKNOWN(4, "不明");  // 掉单

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private String label;

    AdapterTransferStateEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
