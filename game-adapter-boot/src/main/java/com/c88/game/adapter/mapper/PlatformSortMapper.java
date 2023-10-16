package com.c88.game.adapter.mapper;

import com.c88.game.adapter.pojo.entity.PlatformSort;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.game.adapter.pojo.vo.H5PlatformVO;

import java.util.List;

/**
 * @Entity com.c88.game.adapter.pojo.entity.GaPlatformSort
 */
public interface PlatformSortMapper extends BaseMapper<PlatformSort> {

    Boolean modifyPlatformSortTop(Integer id);

    Boolean modifyPlatformSortBottom(Integer id);

    Boolean modifyPlatformSortTopByHot(Integer platformSortId);

    Boolean modifyPlatformSortBottomByHot(Integer platformSortId);

    List<H5PlatformVO> findAllPlatformGameByGameCategory();

    List<H5PlatformVO> findAllHotPlatformGameByGameCategory();
}




