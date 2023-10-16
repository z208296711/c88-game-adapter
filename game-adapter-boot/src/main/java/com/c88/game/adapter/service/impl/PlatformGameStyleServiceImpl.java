package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.mapper.PlatformGameStyleMapper;
import com.c88.game.adapter.pojo.entity.PlatformGameStyle;
import com.c88.game.adapter.pojo.form.ModifyPlatformGameStyleForm;
import com.c88.game.adapter.service.IPlatformGameStyleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * @author user
 * @description 针对表【ga_platform_game_style(遊戲列表樣式)】的数据库操作Service实现
 * @createDate 2022-12-27 11:44:35
 */
@Service
public class PlatformGameStyleServiceImpl extends ServiceImpl<PlatformGameStyleMapper, PlatformGameStyle>
        implements IPlatformGameStyleService {

    @Override
    @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true)
    public Boolean modifyPlatformGameStyle(ModifyPlatformGameStyleForm form) {
        return this.lambdaUpdate()
                .eq(PlatformGameStyle::getGameCategoryCode, form.getGameCategoryCode())
                .set(PlatformGameStyle::getStyleType, form.getStyleType())
                .update();
    }

}




