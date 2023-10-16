package com.c88.game.adapter.service.third.vo.PS;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class PSBetOrderVo {
    @JSONField(name = "wagerId")
    private Integer wagerId;
    @JSONField(name = "eventId")
    private Integer eventId;
    @JSONField(name = "eventName")
    private String eventName;
    @JSONField(name = "wagerDateFm")
    private String wagerDateFm;
    @JSONField(name = "eventDateFm")
    private String eventDateFm;
    @JSONField(name = "settleDateFm")
    private String settleDateFm;
    @JSONField(name = "status")
    private String status;
    @JSONField(name = "homeTeam")
    private String homeTeam;
    @JSONField(name = "awayTeam")
    private String awayTeam;
    @JSONField(name = "selection")
    private String selection;
    @JSONField(name = "handicap")
    private Double handicap;
    @JSONField(name = "odds")
    private Double odds;
    @JSONField(name = "oddsFormat")
    private Integer oddsFormat;
    @JSONField(name = "betType")
    private Integer betType;
    @JSONField(name = "league")
    private String league;
    @JSONField(name = "leagueId")
    private Integer leagueId;
    @JSONField(name = "stake")
    private String stake;
    @JSONField(name = "sportId")
    private Integer sportId;
    @JSONField(name = "sport")
    private String sport;
    @JSONField(name = "currencyCode")
    private String currencyCode;
    @JSONField(name = "inplayScore")
    private String inplayScore;
    @JSONField(name = "inPlay")
    private Boolean inPlay;
    @JSONField(name = "period")
    private Integer period;
    @JSONField(name = "parlaySelections")
    private List<?> parlaySelections;
    @JSONField(name = "toWin")
    private Double toWin;
    @JSONField(name = "toRisk")
    private Integer toRisk;
    @JSONField(name = "product")
    private String product;
    @JSONField(name = "isResettle")
    private Boolean isResettle;
    @JSONField(name = "parlayMixOdds")
    private Double parlayMixOdds;
    @JSONField(name = "wagerType")
    private String wagerType;
    @JSONField(name = "competitors")
    private List<?> competitors;
    @JSONField(name = "userCode")
    private String userCode;
    @JSONField(name = "loginId")
    private String loginId;
    @JSONField(name = "winLoss")
    private String winLoss;
    @JSONField(name = "turnover")
    private Integer turnover;
    @JSONField(name = "scores")
    private List<ScoresDTO> scores;
    @JSONField(name = "result")
    private String result;
    @JSONField(name = "volume")
    private Integer volume;
    @JSONField(name = "view")
    private String view;
    @JSONField(name = "cancellationStatus")
    private String cancellationStatus;

    @NoArgsConstructor
    @Data
    public static class ScoresDTO {
        @JSONField(name = "period")
        private Integer period;
        @JSONField(name = "score")
        private String score;
    }
}
