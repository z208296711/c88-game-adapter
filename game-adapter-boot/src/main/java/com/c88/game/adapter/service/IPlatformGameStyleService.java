package com.c88.game.adapter.service;

import com.c88.game.adapter.pojo.entity.PlatformGameStyle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.form.ModifyPlatformGameStyleForm;

/**
* @author user
* @description 针对表【ga_platform_game_style(遊戲列表樣式)】的数据库操作Service
* @createDate 2022-12-27 11:44:35
*/
public interface IPlatformGameStyleService extends IService<PlatformGameStyle> {

    Boolean modifyPlatformGameStyle(ModifyPlatformGameStyleForm form);
}
