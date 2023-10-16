package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.mapper.SabaVersionsMapper;
import com.c88.game.adapter.pojo.entity.CmdVersions;
import com.c88.game.adapter.pojo.entity.SabaVersions;
import com.c88.game.adapter.service.ISabaVersionsService;

import org.springframework.stereotype.Service;

/**
* @author mac
* @description 针对表【ga_saba_versions(CMD體育抓取版號記錄列表)】的数据库操作Service实现
* @createDate 2022-09-12 14:39:58
*/
@Service
public class SabaVersionsServiceImpl extends ServiceImpl<SabaVersionsMapper, SabaVersions>
    implements ISabaVersionsService {

    @Override
    public SabaVersions getLastVersion() {
        return this.lambdaQuery()
                .eq(SabaVersions::getStatus, GetBetOrdersStatusEnum.VERSION_STATUS_UNDONE.getValue())
                .orderByDesc(SabaVersions::getId)
                .last("limit 1").one();
    }
}




