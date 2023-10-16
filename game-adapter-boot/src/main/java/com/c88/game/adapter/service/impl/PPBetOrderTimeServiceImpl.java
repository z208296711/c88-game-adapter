package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.mapper.PPBetOrderTimeMapper;
import com.c88.game.adapter.pojo.entity.PPBetOrderTime;
import com.c88.game.adapter.service.IPPBetOrderTimeService;
import org.springframework.stereotype.Service;

@Service
public class PPBetOrderTimeServiceImpl extends ServiceImpl<PPBetOrderTimeMapper, PPBetOrderTime>
        implements IPPBetOrderTimeService {

    @Override
    public PPBetOrderTime getLastVersion() {
        return this.lambdaQuery()
                .eq(PPBetOrderTime::getStatus, GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue())
                .orderByDesc(PPBetOrderTime::getId)
                .last("limit 1").one();
    }

}









