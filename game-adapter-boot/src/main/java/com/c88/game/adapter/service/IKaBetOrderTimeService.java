package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.KaBetOrderTime;

/**
 *
 */
public interface IKaBetOrderTimeService extends IService<KaBetOrderTime> {

    KaBetOrderTime getLastVersion();



}
