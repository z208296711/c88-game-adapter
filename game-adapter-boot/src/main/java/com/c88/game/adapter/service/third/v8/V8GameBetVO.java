package com.c88.game.adapter.service.third.v8;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class V8GameBetVO {

    /**
     * 游戏局号列表
     */
    private String transactionId;

    /**
     * 玩家帐号
     */
    private String account;

    /**
     * 游戏局号列表
     */
    private String serverId;

    /**
     * 游戏 ID 列表
     */
    private Integer gameId;

    /**
     * 桌子号列表
     */
    private Long tableId;

    /**
     * 椅子号列表
     */
    private String chairId;

    /**
     * 玩家数量列表
     */
    private String userCount;

    /**
     * 游戏开始时间列表
     */

    private LocalDateTime gameStartTime;

    /**
     * 游戏结束时间列表
     */
    private LocalDateTime gameEndTime;

    /**
     * 渠道 ID 列表
     */
    private String channelId;

    /**
     * 游戏结果对应玩家所属站点
     */
    private String lineCode;

    /**
     * 盈利
     */
    private BigDecimal Profit;

    /**
     * 抽水
     */
    private BigDecimal revenue;

    /**
     * 有效下注 Effective bet
     */
    private BigDecimal validBet;

    /**
     * 总下注列表 Total bet list
     */
    @JSONField(name = "AllBet")
    private BigDecimal allBet;
}
