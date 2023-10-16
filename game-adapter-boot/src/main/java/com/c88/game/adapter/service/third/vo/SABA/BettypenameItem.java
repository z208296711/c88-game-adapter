package com.c88.game.adapter.service.third.vo.SABA;

import com.alibaba.fastjson.annotation.JSONField;

public class BettypenameItem{

	@JSONField(name="name")
	private String name;

	@JSONField(name="lang")
	private String lang;

	public String getName(){
		return name;
	}

	public String getLang(){
		return lang;
	}
}