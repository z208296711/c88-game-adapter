package com.c88.game.adapter.service.third.vo.SABA;

import com.alibaba.fastjson.annotation.JSONField;

public class ParlayDataItem{

	@JSONField(name="bet_team")
	private String betTeam;

	@JSONField(name="ticket_status")
	private String ticketStatus;

	@JSONField(name="islive")
	private String islive;

	@JSONField(name="bet_type")
	private String betType;

	@JSONField(name="winlost_datetime")
	private String winlostDatetime;

	@JSONField(name="home_score")
	private String homeScore;

	@JSONField(name="away_score")
	private String awayScore;

	@JSONField(name="match_id")
	private String matchId;

	@JSONField(name="bet_tag")
	private String betTag;

	@JSONField(name="away_id")
	private String awayId;

	@JSONField(name="away_hdp")
	private String awayHdp;

	@JSONField(name="parlay_id")
	private String parlayId;

	@JSONField(name="hdp")
	private String hdp;

	@JSONField(name="odds")
	private String odds;

	@JSONField(name="home_id")
	private String homeId;

	@JSONField(name="match_datetime")
	private String matchDatetime;

	@JSONField(name="league_id")
	private String leagueId;

	@JSONField(name="home_hdp")
	private String homeHdp;

	@JSONField(name="sport_type")
	private String sportType;

	public String getBetTeam(){
		return betTeam;
	}

	public String getTicketStatus(){
		return ticketStatus;
	}

	public String getIslive(){
		return islive;
	}

	public String getBetType(){
		return betType;
	}

	public String getWinlostDatetime(){
		return winlostDatetime;
	}

	public String getHomeScore(){
		return homeScore;
	}

	public String getAwayScore(){
		return awayScore;
	}

	public String getMatchId(){
		return matchId;
	}

	public String getBetTag(){
		return betTag;
	}

	public String getAwayId(){
		return awayId;
	}

	public String getAwayHdp(){
		return awayHdp;
	}

	public String getParlayId(){
		return parlayId;
	}

	public String getHdp(){
		return hdp;
	}

	public String getOdds(){
		return odds;
	}

	public String getHomeId(){
		return homeId;
	}

	public String getMatchDatetime(){
		return matchDatetime;
	}

	public String getLeagueId(){
		return leagueId;
	}

	public String getHomeHdp(){
		return homeHdp;
	}

	public String getSportType(){
		return sportType;
	}
}