package com.c88.game.adapter.service.third.vo;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MPGameBetVO {

    /**
     * 游戏局号列表
     */
    private String gameId;

    /**
     * 玩家帐号列表
     */
    private String account;

    /**
     * 房间 ID 列表
     */
    private Integer serverId;

    /**
     * 游戏 ID 列表
     */
    private Integer kindId;

    /**
     * 桌子号列表
     */
    private Integer tableId;

    /**
     * 椅子号列表
     */
    private Integer chairId;

    /**
     * 玩家数量列表
     */
    private Integer userCount;

    /**
     * 手牌公共牌
     */
    private String cardValue;

    /**
     * 有效下注
     */
    private BigDecimal cellScore;

    /**
     * 总下注列表 Total bet list
     */
    @JSONField(name = "AllBet")
    private BigDecimal allBet;

    /**
     * 盈利列表
     */
    private BigDecimal profit;

    /**
     * 抽水列表
     */
    private BigDecimal revenue;

    /**
     * 结算后玩家余额
     */
    private BigDecimal newScore;

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
    private Integer channelId;

    /**
     * 游戏结果对应玩家所属站点
     */
    private String lineCode;


}
