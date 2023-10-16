package com.c88.game.adapter.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.c88.game.adapter.pojo.entity.CmdVersions;
import com.c88.game.adapter.pojo.entity.FetchRecord;
import com.c88.game.adapter.service.IRetryFetchOrder;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import com.c88.game.adapter.template.RetryTemplate;
import org.springframework.stereotype.Service;


@Service
public class RetryFetchOrderImpl implements IRetryFetchOrder {

    public Object retryFetchOrderTime(FetchRecord errorBetOrder){
        try {
            return new RetryTemplate() {
                @Override
                protected Object doBiz(){
                    IGameAdapter gameAdapter = SpringUtil.getBean(GameAdapterExecutor.class).findByGamePlatFormByCode(errorBetOrder.getPlatformCode());
                    return gameAdapter.fetchBetOrder(errorBetOrder.getStartTime(), errorBetOrder.getEndTime());
                }
            }.execute();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object retryFetchOrderVersion(CmdVersions errorBetOrder) {
        try {
            return new RetryTemplate() {
                @Override
                protected Object doBiz(){
                    IGameAdapter gameAdapter = SpringUtil.getBean(GameAdapterExecutor.class).findByGamePlatFormByCode(errorBetOrder.getPlatformCode());
                    return gameAdapter.fetchBetOrderVersion(errorBetOrder.getVersion());
                }
            }.execute();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
