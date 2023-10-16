package com.c88.game.adapter.service.third.vo.SABA;

import com.alibaba.fastjson.annotation.JSONField;

public class SingleParlayDataItem{

	@JSONField(name="selection_name")
	private String selectionName;

	@JSONField(name="selection_name_cs")
	private String selectionNameCs;

	@JSONField(name="status")
	private String status;

	public String getSelectionName(){
		return selectionName;
	}

	public String getSelectionNameCs(){
		return selectionNameCs;
	}

	public String getStatus(){
		return status;
	}
}