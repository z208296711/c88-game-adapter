package com.c88.game.adapter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.game.adapter.pojo.entity.GameCategory;
import com.c88.member.vo.OptionVO;

import java.util.List;

/**
* @author user
* @description 针对表【game_template(遊戲類型)】的数据库操作Mapper
* @createDate 2022-05-10 13:44:33
* @Entity data.entity.GameTemplate
*/
public interface GameCategoryMapper extends BaseMapper<GameCategory> {

    List<GameCategory> getGameCategoryByPlatformId(int platformId);

}




