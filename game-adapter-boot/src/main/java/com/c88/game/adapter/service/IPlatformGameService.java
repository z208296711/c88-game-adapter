package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.dto.GameVO;
import com.c88.game.adapter.pojo.entity.PlatformGame;
import com.c88.game.adapter.pojo.form.*;
import com.c88.game.adapter.pojo.vo.PlatformGameSortVO;
import com.c88.game.adapter.pojo.vo.PlatformGameVO;

import java.util.List;
import java.util.Map;

/**
 * @author user
 * @description 针对表【platform_game(平台遊戲列表)】的数据库操作Service
 * @createDate 2022-05-10 13:46:35
 */
public interface IPlatformGameService extends IService<PlatformGame> {

    IPage<PlatformGameVO> findPlatformGame(FindPlatformGameForm form);

    Boolean addPlatformGame(AddPlatformGameForm form);

    Boolean modifyPlatformGameForm(ModifyPlatformGameForm form);

    PlatformGame getPlatformGameById(Integer id);

    List<PlatformGame> getPlatformGameByPlatform(String platformCode);

    PlatformGame getPlatformGameByCode(String platformCode, String gameId);

    Boolean deletePlatformGame(List<Integer> ids);

    IPage<PlatformGameSortVO> findPlatformGameSort(FindPlatformGameSortForm form);

    Boolean modifyPlatformGameSort(ModifyPlatformGameSortForm form);

    Boolean modifyPlatformGameSortTopBottom(ModifyPlatformGameSortTopBottomForm form);

    Map<String, List<GameVO>> getGameListByPlatforms(List<String> platformCodeX);
}
