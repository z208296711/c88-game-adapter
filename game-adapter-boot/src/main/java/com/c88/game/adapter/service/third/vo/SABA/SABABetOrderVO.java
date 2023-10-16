package com.c88.game.adapter.service.third.vo.SABA;

import java.util.List;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class SABABetOrderVO{

	@JSONField(name="BetNumberDetails")
	private List<BetNumberDetailsItem> betNumberDetails;

	@JSONField(name="BetDetails")
	private List<BetDetailsItem> betDetails;

	@JSONField(name="BetVirtualSportDetails")
	private List<BetVirtualSportDetailsItem> betVirtualSportDetails;

	@JSONField(name="last_version_key")
	private Long lastVersionKey;

//	public List<BetNumberDetailsItem> getBetNumberDetails(){
//		return betNumberDetails;
//	}
//
//	public List<BetDetailsItem> getBetDetails(){
//		return betDetails;
//	}
//
//	public List<BetVirtualSportDetailsItem> getBetVirtualSportDetails(){
//		return betVirtualSportDetails;
//	}
}