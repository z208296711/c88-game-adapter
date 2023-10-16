package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.mapper.Cq9BetOrderTimeMapper;
import com.c88.game.adapter.pojo.entity.Cq9BetOrderTime;
import com.c88.game.adapter.service.ICq9BetOrderTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Cq9BetOrderTimeServiceImpl extends ServiceImpl<Cq9BetOrderTimeMapper, Cq9BetOrderTime>
        implements ICq9BetOrderTimeService {

    @Override
    public Cq9BetOrderTime getLastVersion() {
        return this.lambdaQuery()
                .eq(Cq9BetOrderTime::getStatus, GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue())
                .orderByDesc(Cq9BetOrderTime::getGmtModified)
                .last("limit 1").one();
    }
}
