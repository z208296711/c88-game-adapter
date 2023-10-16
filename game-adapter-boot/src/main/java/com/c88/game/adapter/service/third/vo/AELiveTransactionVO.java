package com.c88.game.adapter.service.third.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AELiveTransactionVO {

    /**
     * 平台游戏类型
     */
    @JSONField(name = "gameType")
    private String gameType;

    /**
     * 返还金额 (包含下注金额)
     */
    @JSONField(name = "winAmount")
    private BigDecimal winAmount;

    /**
     * 用于区分注单结果是否有更改
     * 正常状况：
     * 预设：0
     * 结果更改过状况：
     * Unsettle / Voidsettle / Unvoidsettle: 1
     * Voidbet: -1
     */
    @JSONField(name = "settleStatus")
    private Integer settleStatus;

    /**
     * 真实下注金额
     */
    @JSONField(name = "realBetAmount")
    private BigDecimal realBetAmount;

    /**
     * 真实返还金额
     */
    @JSONField(name = "realWinAmount")
    private BigDecimal realWinAmount;

    /**
     * 辨认交易时间依据
     */
    @JSONField(name = "txTime")
    private String txTime;

    /**
     * 更新时间 (遵循 ISO8601 格式)
     * 请使用拉取最后一张注单的更新时间当做取下一次拉帐的 timeFrom 参数
     * 注意：若某次取值无资料 或 无更新资料，则将下次取值 timeFrom 设为现在时间的前一分钟
     */
    @JSONField(name = "updateTime")
    private String updateTime;

    /**
     * 玩家 ID
     */
    @JSONField(name = "userId")
    private String userId;

    /**
     * 游戏平台的下注项目
     */
    @JSONField(name = "betType")
    private String betType;

    /**
     * 游戏平台名称
     */
    @JSONField(name = "platform")
    private String platform;

    /**
     * 交易状态
     * 若无带入参数则默认回传数值包含以下：
     * -1 Canceled 已取消投注※
     * 1 Settled 已结账
     * 2 Void 注单无效
     * 3 SCRATCH in Horsebook 赛马游戏割马后退回的金额
     * 5 Refund bet of Place in HRB 退还赛马交易下注"位置"的金额
     * 9 Invalid 无效交易
     */
    @JSONField(name = "txStatus")
    private Integer txStatus;

    /**
     * 下注金额
     */
    @JSONField(name = "betAmount")
    private BigDecimal betAmount;

    /**
     * 游戏名称
     */
    @JSONField(name = "gameName")
    private String gameName;

    /**
     * 游戏商注单号
     */
    @JSONField(name = "platformTxId")
    private String platformTxId;

    /**
     * 玩家下注时间
     */
    @JSONField(name = "betTime")
    private String betTime;

    /**
     * 平台游戏代码
     */
    @JSONField(name = "gameCode")
    private String gameCode;

    /**
     * 玩家货币代码
     */
    @JSONField(name = "currency")
    private String currency;

    /**
     * 累积奖金的下注金额
     */
    @JSONField(name = "jackpotBetAmount")
    private BigDecimal jackpotBetAmount;

    /**
     * 累积奖金的获胜金额
     */
    @JSONField(name = "jackpotWinAmount")
    private BigDecimal jackpotWinAmount;

    /**
     * 游戏平台有效投注
     */
    @JSONField(name = "turnover")
    private BigDecimal turnover;

    /**
     * 游戏商的回合识别码
     * Example 范例：Mexico-01-GA17590001
     */
    @JSONField(name = "roundId")
    private String roundId;

    /**
     * 游戏讯息会由游戏商以 JSON 格式提供
     */
    @JSONField(name = "gameInfo")
    private JSONObject gameInfo;

}
