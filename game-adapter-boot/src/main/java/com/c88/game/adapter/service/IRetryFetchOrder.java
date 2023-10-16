package com.c88.game.adapter.service;

import com.c88.game.adapter.pojo.entity.CmdVersions;
import com.c88.game.adapter.pojo.entity.FetchRecord;

public interface IRetryFetchOrder {

    Object retryFetchOrderTime(FetchRecord errorBetOrder);

    Object retryFetchOrderVersion(CmdVersions errorBetOrder);
}
