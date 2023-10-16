package com.c88.game.adapter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.game.adapter.pojo.entity.PlatformGame;

/**
* @author user
* @description 针对表【platform_game(平台遊戲列表)】的数据库操作Mapper
* @createDate 2022-05-10 13:46:35
* @Entity data.entity.PlatformGame
*/
public interface PlatformGameMapper extends BaseMapper<PlatformGame> {

    Boolean modifyPlatformGameTop(Integer id);

    Boolean modifyPlatformGameBottom(Integer id);
}




