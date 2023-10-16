package com.c88.game.adapter.service;

import com.c88.game.adapter.pojo.entity.CmdVersions;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface ICmdVersionsService extends IService<CmdVersions> {

    CmdVersions getLastVersion(String platformCode);



}
