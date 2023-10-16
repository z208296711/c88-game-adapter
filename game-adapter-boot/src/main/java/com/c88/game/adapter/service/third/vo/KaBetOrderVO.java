package com.c88.game.adapter.service.third.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KaBetOrderVO {

    private String playerId;
    private String transactionId;
    private String gameName;
    private String gameType;
    private LocalDateTime spinDate;// 指定时区下玩家该局的下注时间
    private int tzOffset;// 0(UTC+0) default
    private String currency;
    private float rtp;// 游戏的玩家回报率
    private float denomination;// 积分面值
    private int selections;// 赔付线游戏的下注线数或方式游戏的转轮数
    private long creditPerSelection;// 赔付线游戏的单线下注积分或方式游戏的下注积分倍数
    private long cashPlayed;// 玩家该局的下注金额，以分为单位
    private long cashPlayedRaw;// 玩家该局的下注金额，以分为单位该数据不包含合作伙伴的货币乘数
    private long cashWon;// 玩家该局赢得派彩，以分为单位
    private long cashWonRaw;// 玩家该局赢得派彩，以分为单位该数据不包含合作伙伴的货币乘数
    private long creditsPlayed;// 玩家该局的下注积分
    private long creditsWon;// 玩家该局赢得积分
    private boolean freeSpin;// True/False，是否为免费游戏。True 表示免费游戏，False 表示一般游戏
    private int round;// 该局为免费游戏的第几回合
    private int roundsRemaining;// 该局游戏后，剩余的免费游戏次数

    public long getFreePlayed(){
        return (freeSpin ? 0 : cashPlayed);
    }
}
