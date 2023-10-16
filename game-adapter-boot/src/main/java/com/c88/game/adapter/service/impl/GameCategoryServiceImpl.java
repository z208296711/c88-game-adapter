package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.LogOpResponse;
import com.c88.common.web.log.OperationEnum;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.mapper.GameCategoryMapper;
import com.c88.game.adapter.mapstruct.GameCategoryConverter;
import com.c88.game.adapter.pojo.entity.GameCategory;
import com.c88.game.adapter.pojo.form.ModifyGameCategoryNoteForm;
import com.c88.game.adapter.pojo.form.ModifyGameCategorySortForm;
import com.c88.game.adapter.pojo.vo.GameCategoryVO;
import com.c88.game.adapter.service.IGameCategoryService;
import com.c88.member.vo.OptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author user
 * @description 针对表【game_template(遊戲類型)】的数据库操作Service实现
 * @createDate 2022-05-10 13:44:33
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameCategoryServiceImpl extends ServiceImpl<GameCategoryMapper, GameCategory>
        implements IGameCategoryService {

    private final GameCategoryConverter gameCategoryConverter;

    private final GameCategoryMapper gameCategoryMapper;

    @Override
    public List<GameCategoryVO> findGameCategory() {
        return this.list()
                .stream()
                .map(gameCategoryConverter::toVo)
                .sorted(Comparator.comparingInt(GameCategoryVO::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean modifyGameCategoryNote(ModifyGameCategoryNoteForm form) {
        return this.lambdaUpdate()
                .eq(GameCategory::getId, form.getId())
                .set(GameCategory::getNote, form.getNote())
                .update();
    }

    @Override
    public List<OptionVO> getGameCategoryByGame(int platformId) {
        return gameCategoryMapper.getGameCategoryByPlatformId(platformId)
                .stream()
                .map(platform -> OptionVO.<String>builder()
                        .value(String.valueOf(platform.getId()))
                        .label(platform.getName())
                        .build()
                )
                .collect(Collectors.toUnmodifiableList());
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true)
    })
    @Override
    public Boolean modifyGameCategorySort(ModifyGameCategorySortForm form) {
        return this.updateBatchById(
                form.getGameAdapterSortRequests()
                        .stream()
                        .map(x ->
                                GameCategory.builder()
                                        .id(x.getId())
                                        .sort(x.getSort())
                                        .build()
                        )
                        .collect(Collectors.toUnmodifiableList())
        );
    }

}




