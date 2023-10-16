package com.c88.game.adapter.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;


public enum PPBetInfoStatusEnum implements IBaseEnum<Integer> {

    BET_STATUS_PROCESS("I", 0, "未派彩"),
    BET_STATUS_SETTLED("C", 1, "已派彩");

    @Getter
    private String status;

    @Getter
    private Integer value;

    @Getter
    private String label;

    PPBetInfoStatusEnum(String status, Integer value, String label) {
        this.status = status;
        this.value = value;
        this.label = label;
    }

}
