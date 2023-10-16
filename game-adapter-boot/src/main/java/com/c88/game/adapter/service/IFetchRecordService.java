package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.FetchRecord;


public interface IFetchRecordService extends IService<FetchRecord> {

    FetchRecord getLastVersion(String platformCode);

}
