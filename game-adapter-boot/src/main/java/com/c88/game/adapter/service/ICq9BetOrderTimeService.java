package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.Cq9BetOrderTime;

public interface ICq9BetOrderTimeService extends IService<Cq9BetOrderTime> {

    Cq9BetOrderTime getLastVersion();
}
