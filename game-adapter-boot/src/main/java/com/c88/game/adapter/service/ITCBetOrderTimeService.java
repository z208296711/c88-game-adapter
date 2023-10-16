package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.TCBetOrderTime;

/**
 *
 */
public interface ITCBetOrderTimeService extends IService<TCBetOrderTime> {

    TCBetOrderTime getLastVersion();



}