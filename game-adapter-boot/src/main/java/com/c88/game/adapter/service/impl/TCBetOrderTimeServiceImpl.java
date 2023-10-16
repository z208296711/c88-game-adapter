package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.mapper.TCBetOrderTimeMapper;
import com.c88.game.adapter.pojo.entity.TCBetOrderTime;
import com.c88.game.adapter.service.ITCBetOrderTimeService;
import org.springframework.stereotype.Service;

@Service
public class TCBetOrderTimeServiceImpl extends ServiceImpl<TCBetOrderTimeMapper, TCBetOrderTime>
        implements ITCBetOrderTimeService {

    @Override
    public TCBetOrderTime getLastVersion() {
        return this.lambdaQuery()
                .eq(TCBetOrderTime::getStatus, GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue())
                .orderByDesc(TCBetOrderTime::getId)
                .last("limit 1").one();
    }

}