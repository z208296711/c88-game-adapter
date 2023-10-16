package com.c88.game.adapter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.vo.PlatformSortVO;

import java.util.List;

/**
* @author user
* @description 针对表【platform(遊戲平台)】的数据库操作Mapper
* @createDate 2022-05-10 13:46:31
* @Entity data.entity.Platform
*/
public interface PlatformMapper extends BaseMapper<Platform> {

    List<PlatformSortVO> findPlatformSort(Integer gameCategoryId);

    List<PlatformSortVO> findPlatformSortByHot();
}




