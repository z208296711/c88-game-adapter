package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.pojo.entity.FetchRecord;
import com.c88.game.adapter.pojo.entity.KaBetOrderTime;
import com.c88.game.adapter.service.IFetchRecordService;
import com.c88.game.adapter.mapper.FetchRecordMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class FetchRecordServiceImpl extends ServiceImpl<FetchRecordMapper, FetchRecord> implements IFetchRecordService {

    @Override
    public FetchRecord getLastVersion(String platformCode) {
        return this.lambdaQuery()
                .eq(FetchRecord::getStatus, GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue())
                .eq(FetchRecord::getPlatformCode, platformCode)
                .orderByDesc(FetchRecord::getId)
                .last("limit 1").one();
    }
}




