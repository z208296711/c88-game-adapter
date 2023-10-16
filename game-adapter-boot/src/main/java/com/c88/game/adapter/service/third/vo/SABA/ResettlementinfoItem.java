package com.c88.game.adapter.service.third.vo.SABA;

import com.alibaba.fastjson.annotation.JSONField;

public class ResettlementinfoItem{

	@JSONField(name="ticket_status")
	private String ticketStatus;

	@JSONField(name="winlost")
	private String winlost;

	@JSONField(name="balancechange")
	private String balancechange;

	@JSONField(name="action")
	private String action;

	@JSONField(name="actionDate")
	private String actionDate;

	public String getTicketStatus(){
		return ticketStatus;
	}

	public String getWinlost(){
		return winlost;
	}

	public String getBalancechange(){
		return balancechange;
	}

	public String getAction(){
		return action;
	}

	public String getActionDate(){
		return actionDate;
	}
}