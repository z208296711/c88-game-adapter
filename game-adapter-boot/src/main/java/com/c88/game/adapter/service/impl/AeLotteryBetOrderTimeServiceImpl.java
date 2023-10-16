package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.mapper.AeLotteryBetOrderTimeMapper;
import com.c88.game.adapter.pojo.entity.AeLotteryBetOrderTime;
import com.c88.game.adapter.service.ILotteryAeBetOrderTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AeLotteryBetOrderTimeServiceImpl extends ServiceImpl<AeLotteryBetOrderTimeMapper, AeLotteryBetOrderTime>
        implements ILotteryAeBetOrderTimeService {

}
