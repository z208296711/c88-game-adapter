package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.mapper.CmdVersionsMapper;
import com.c88.game.adapter.pojo.entity.CmdVersions;
import com.c88.game.adapter.service.ICmdVersionsService;
import org.springframework.stereotype.Service;

@Service
public class CmdVersionsServiceImpl extends ServiceImpl<CmdVersionsMapper, CmdVersions>
    implements ICmdVersionsService {

    @Override
    public CmdVersions getLastVersion(String platformCode) {
        return this.lambdaQuery()
                .eq(CmdVersions::getStatus, GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue())
                .eq(CmdVersions::getPlatformCode,platformCode )
                .orderByDesc(CmdVersions::getId)
                .last("limit 1").one();
    }
}




