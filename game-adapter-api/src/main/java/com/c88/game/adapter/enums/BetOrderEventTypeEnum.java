package com.c88.game.adapter.enums;

import lombok.Getter;


public enum BetOrderEventTypeEnum {

    BET_ALREADY_PULL(-2, "重複拉取, 資料相同"),
    BET_NEW_ORDER(0, "新注單"),
    BET_SETTLED(1, "結算注單"),
    BET_UPDATE_SETTLE(2, "注單狀態有變，更新注單"),
    BET_CANCELED(-1, "舊注單-已取消");

    @Getter
    private Integer value;

    @Getter
    private String desc;


    BetOrderEventTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

}
