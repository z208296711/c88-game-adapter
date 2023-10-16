package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.mapper.KaBetOrderTimeMapper;
import com.c88.game.adapter.pojo.entity.KaBetOrderTime;
import com.c88.game.adapter.service.IKaBetOrderTimeService;
import org.springframework.stereotype.Service;

@Service
public class KaBetOrderTimeServiceImpl extends ServiceImpl<KaBetOrderTimeMapper, KaBetOrderTime>
        implements IKaBetOrderTimeService {

    @Override
    public KaBetOrderTime getLastVersion() {
        return this.lambdaQuery()
                .eq(KaBetOrderTime::getStatus, GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue())
                .orderByDesc(KaBetOrderTime::getId)
                .last("limit 1").one();
    }
}




