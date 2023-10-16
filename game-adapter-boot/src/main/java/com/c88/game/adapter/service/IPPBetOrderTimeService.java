package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.PPBetOrderTime;

/**
 *
 */
public interface IPPBetOrderTimeService extends IService<PPBetOrderTime> {

    PPBetOrderTime getLastVersion();



}
