package com.c88.game.adapter.service.third.vo;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AELotteryBetOrderListVO {

    /**
     * 幣別
     */
    @JSONField(name = "currency")
    private String currency;

    /**
     * 會員ID
     */
    @JSONField(name = "user_id")
    private String userId;

    /**
     * 會員帳號
     */
    @JSONField(name = "account")
    private String account;

    /**
     * 會員類型
     * 0: 正式 1:測試
     */
    @JSONField(name = "user_level")
    private Integer userLevel;

    /**
     * 注單IP
     */
    @JSONField(name = "ip")
    private String ip;

    /**
     * 遊戲ID
     */
    @JSONField(name = "game_id")
    private String gameId;

    /**
     * 遊戲期數
     */
    @JSONField(name = "game_round")
    private String gameRound;

    /**
     * 投注時間
     */
    @JSONField(name = "bet_at")
    private Long betAt;

    /**
     * 更新時間
     */
    @JSONField(name = "updated_at")
    private Long updatedAt;

    /**
     * 結算時間
     */
    @JSONField(name = "settled_at")
    private Long settledAt;

    /**
     * 重新結算時間
     */
    @JSONField(name = "resettled_at")
    private Long resettledAt;

    /**
     * 派彩時間
     */
    @JSONField(name = "paid_at")
    private Long paidAt;

    /**
     * 重新派彩時間
     */
    @JSONField(name = "repaid_at")
    private Long repaidAt;

    /**
     * 註銷時間
     */
    @JSONField(name = "canceled_at")
    private Long canceledAt;

    /**
     * 投注單號
     */
    @JSONField(name = "order_id")
    private String orderId;

    /**
     * 注單狀態
     * 1:下注、2:退款、3: 結算 、 4:派彩 、 5:重新結算 、 6:重新派彩、7:註銷
     */
    @JSONField(name = "status")
    private Integer status;

    /**
     * 下注內容
     */
    @JSONField(name = "bet_info")
    private AELotteryBetOrderInfoVO betInfo;

    /**
     * 玩法
     */
    @JSONField(name = "wager")
    private String wager;

    /**
     * 玩法的投注項目
     */
    @JSONField(name = "wager_bet_item")
    private String wagerBetItem;

    /**
     * 投注金額
     */
    @JSONField(name = "bet_amount")
    private BigDecimal betAmount;

    /**
     * 派彩金額
     */
    @JSONField(name = "paid_amount")
    private BigDecimal paidAmount;

    /**
     * 玩家損益
     */
    @JSONField(name = "player_profit_amount")
    private BigDecimal playerProfitAmount;

    /**
     * 有效投注
     */
    @JSONField(name = "valid_bet")
    private BigDecimal validBet;

}
