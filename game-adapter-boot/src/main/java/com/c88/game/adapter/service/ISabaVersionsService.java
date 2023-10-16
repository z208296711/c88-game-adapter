package com.c88.game.adapter.service;

import com.c88.game.adapter.pojo.entity.SabaVersions;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author mac
* @description 针对表【ga_saba_versions(CMD體育抓取版號記錄列表)】的数据库操作Service
* @createDate 2022-09-12 14:39:58
*/
public interface ISabaVersionsService extends IService<SabaVersions> {
    SabaVersions getLastVersion();
}
