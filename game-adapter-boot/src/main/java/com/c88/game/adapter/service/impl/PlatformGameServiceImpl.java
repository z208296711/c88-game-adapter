package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.dto.GameVO;
import com.c88.game.adapter.enums.SortTypeEnum;
import com.c88.game.adapter.mapper.PlatformGameMapper;
import com.c88.game.adapter.mapstruct.PlatformGameConverter;
import com.c88.game.adapter.mapstruct.PlatformGameSortConverter;
import com.c88.game.adapter.mapstruct.PlatformGameToRepositoryConverter;
import com.c88.game.adapter.pojo.entity.GameCategory;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGame;
import com.c88.game.adapter.pojo.form.*;
import com.c88.game.adapter.pojo.vo.PlatformGameSortVO;
import com.c88.game.adapter.pojo.vo.PlatformGameVO;
import com.c88.game.adapter.repository.IPlatformGameRepository;
import com.c88.game.adapter.service.IGameCategoryService;
import com.c88.game.adapter.service.IPlatformGameService;
import com.c88.game.adapter.service.IPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author user
 * @description 针对表【platform_game(平台遊戲列表)】的数据库操作Service实现
 * @createDate 2022-05-10 13:46:35
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformGameServiceImpl extends ServiceImpl<PlatformGameMapper, PlatformGame> implements IPlatformGameService {

    private final PlatformGameConverter platformGameConverter;

    private final PlatformGameSortConverter platformGameSortConverter;

    private final PlatformGameToRepositoryConverter platformGameToRepositoryConverter;

    private final IPlatformGameRepository iPlatformGameRepository;

    private final IPlatformService iPlatformService;

    private final IGameCategoryService iGameCategoryService;

    public IPage<PlatformGameVO> findPlatformGame(FindPlatformGameForm form) {
        return this.lambdaQuery()
                .eq(Objects.nonNull(form.getPlatformId()), PlatformGame::getPlatformId, form.getPlatformId())
                .eq(Objects.nonNull(form.getGameCategoryId()), PlatformGame::getGameCategoryId, form.getGameCategoryId())
                .eq(Objects.nonNull(form.getEnable()), PlatformGame::getEnable, form.getEnable())
                .eq(Objects.nonNull(form.getGameType()) && form.getGameType() == 0, PlatformGame::getHotFlag, 1)
                .eq(Objects.nonNull(form.getGameType()) && form.getGameType() == 1, PlatformGame::getRecommendFlag, 1)
                .and(StringUtils.hasText(form.getKeyword()), wrapper -> wrapper
                        .like(StringUtils.hasText(form.getKeyword()), PlatformGame::getNameVi, form.getKeyword())
                        .or(StringUtils.hasText(form.getKeyword()))
                        .like(StringUtils.hasText(form.getKeyword()), PlatformGame::getNameEn, form.getKeyword())
                        .or(StringUtils.hasText(form.getKeyword()))
                        .like(StringUtils.hasText(form.getKeyword()), PlatformGame::getPcCode1, form.getKeyword())
                        .or(StringUtils.hasText(form.getKeyword()))
                        .like(StringUtils.hasText(form.getKeyword()), PlatformGame::getPcCode2, form.getKeyword())
                        .or(StringUtils.hasText(form.getKeyword()))
                        .like(StringUtils.hasText(form.getKeyword()), PlatformGame::getH5Code1, form.getKeyword())
                        .or(StringUtils.hasText(form.getKeyword()))
                        .like(StringUtils.hasText(form.getKeyword()), PlatformGame::getH5Code2, form.getKeyword()))
                .orderByDesc(PlatformGame::getId)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformGameConverter::toVo);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true)
    })
    @Transactional
    @Override
    public Boolean addPlatformGame(AddPlatformGameForm form) {
        Platform platform = iPlatformService.getPlatformById(form.getPlatformId());

        GameCategory gameCategory = iGameCategoryService.getById(form.getGameCategoryId());

        PlatformGame platformGame = PlatformGame.builder()
                .nameVi(form.getNameVi())
                .nameEn(form.getNameEn())
                .platformName(platform.getName())
                .gameCategoryName(gameCategory.getName())
                .platformId(form.getPlatformId())
                .gameCategoryId(form.getGameCategoryId())
                .pcDeviceVisible(form.getPcDeviceVisible())
                .h5DeviceVisible(form.getH5DeviceVisible())
                .gameId(form.getGameId())
                .extendField(form.getExtendField())
                .tags(StringUtils.hasText(form.getTags()) ? List.of(form.getTags()) : null)
                .hotFlag(form.getHotFlag())
                .recommendFlag(form.getRecommendFlag())
                .pcCode1(form.getPcCode1())
                .pcCode2(form.getPcCode2())
                .h5Code1(form.getH5Code1())
                .h5Code2(form.getH5Code2())
                .gameImage(StringUtils.hasText(form.getGameImage()) ? form.getGameImage() : null)
                .recommendImage(StringUtils.hasText(form.getRecommendImage()) ? form.getRecommendImage() : null)
                .build();

        boolean isSuccess = this.save(platformGame);

        if (isSuccess) {
            iPlatformGameRepository.save(platformGameToRepositoryConverter.toVo(this.getPlatformGameById(platformGame.getId())));
            isSuccess = this.baseMapper.modifyPlatformGameBottom(platformGame.getId());
        }

        return isSuccess;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_BY_GAME_ID, allEntries = true),
            @CacheEvict(cacheNames = "PNG", allEntries = true)
    })
    @Transactional
    @Override
    public Boolean modifyPlatformGameForm(ModifyPlatformGameForm form) {
        log.info("upload_image:{}", form.getGameImage());
        PlatformGame platformGame = PlatformGame.builder()
                .id(form.getId())
                .nameVi(form.getNameVi())
                .nameEn(form.getNameEn())
                .platformId(form.getPlatformId())
                .gameCategoryId(form.getGameCategoryId())
                .pcDeviceVisible(form.getPcDeviceVisible())
                .h5DeviceVisible(form.getH5DeviceVisible())
                .gameId(form.getGameId())
                .extendField(form.getExtendField())
                .tags(StringUtils.hasText(form.getTags()) ? List.of(form.getTags()) : null)
                .hotFlag(form.getHotFlag())
                .recommendFlag(form.getRecommendFlag())
                .pcCode1(form.getPcCode1())
                .pcCode2(form.getPcCode2())
                .h5Code1(form.getH5Code1())
                .h5Code2(form.getH5Code2())
                .enable(form.getEnable())
                .gameImage(form.getGameImage() != null ? form.getGameImage() : null)
                .recommendImage(form.getRecommendImage() != null ? form.getRecommendImage() : null)
                .build();

        if (Objects.equals(form.getGameImage(), "")) {
            platformGame.setGameImage("");
        }
        if (Objects.equals(form.getRecommendImage(), "")) {
            platformGame.setRecommendImage("");
        }

        if (Objects.nonNull(form.getPlatformId())) {
            Platform platform = iPlatformService.getPlatformById(form.getPlatformId());
            platformGame.setPlatformName(platform.getName());
        }

        if (Objects.nonNull(form.getGameCategoryId())) {
            GameCategory gameCategory = iGameCategoryService.getById(form.getGameCategoryId());
            platformGame.setGameCategoryName(gameCategory.getName());
        }

        boolean isSuccess = this.updateById(platformGame);
        if (isSuccess) {
            iPlatformGameRepository.save(platformGameToRepositoryConverter.toVo(this.getPlatformGameById(form.getId())));
        }

        return isSuccess;
    }

    //    @Cacheable(cacheNames = RedisKey.PLATFORM_GAME_BY_GAME_ID, key = "#platformCode + #gameId")
    public PlatformGame getPlatformGameByCode(String platformCode, String gameId) {
        return this.lambdaQuery()
                .eq(PlatformGame::getPlatformName, platformCode)
                .eq(org.apache.commons.lang3.StringUtils.isNotEmpty(gameId), PlatformGame::getGameId, gameId)
                .one();
    }

    public List<PlatformGame> getPlatformGameByPlatform(String platformCode) {
        return this.lambdaQuery()
                .eq(PlatformGame::getPlatformName, platformCode)
                .list();
    }

    @Cacheable(cacheNames = RedisKey.PLATFORM_GAME_BY_ID, key = "#id")
    public PlatformGame getPlatformGameById(Integer id) {
        return Optional.ofNullable(this.getById(id))
                .orElseThrow(() -> new RuntimeException("無此平台遊戲"));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true),
            @CacheEvict(cacheNames = "PNG", allEntries = true)
    })
    @Transactional
    @Override
    public Boolean deletePlatformGame(List<Integer> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public IPage<PlatformGameSortVO> findPlatformGameSort(FindPlatformGameSortForm form) {
        return this.lambdaQuery()
                .eq(PlatformGame::getPlatformId, form.getPlatformId())
                .eq(PlatformGame::getGameCategoryId, form.getGameCategoryId())
                .and(StringUtils.hasText(form.getName()), wrapper -> wrapper
                        .like(StringUtils.hasText(form.getName()), PlatformGame::getNameVi, form.getName())
                        .or(StringUtils.hasText(form.getName()))
                        .like(StringUtils.hasText(form.getName()), PlatformGame::getNameEn, form.getName())
                        .or(StringUtils.hasText(form.getName()))
                        .like(StringUtils.hasText(form.getName()), PlatformGame::getPcCode1, form.getName())
                        .or(StringUtils.hasText(form.getName()))
                        .like(StringUtils.hasText(form.getName()), PlatformGame::getPcCode2, form.getName())
                        .or(StringUtils.hasText(form.getName()))
                        .like(StringUtils.hasText(form.getName()), PlatformGame::getH5Code1, form.getName())
                        .or(StringUtils.hasText(form.getName()))
                        .like(StringUtils.hasText(form.getName()), PlatformGame::getH5Code2, form.getName()))
                .orderByAsc(PlatformGame::getGameSort)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformGameSortConverter::toVo);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true)
    })
    @Override
    public Boolean modifyPlatformGameSort(ModifyPlatformGameSortForm form) {
        return this.updateBatchById(
                form.getGameAdapterSortRequests()
                        .stream()
                        .map(x ->
                                PlatformGame.builder()
                                        .id(x.getId())
                                        .gameSort(x.getSort())
                                        .build()
                        )
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true)
    })
    @Override
    public Boolean modifyPlatformGameSortTopBottom(ModifyPlatformGameSortTopBottomForm form) {
        SortTypeEnum sortType = SortTypeEnum.getEnum(form.getSortType());
        switch (sortType) {
            case TOP:
                return this.baseMapper.modifyPlatformGameTop(form.getId());
            case BOTTOM:
                return this.baseMapper.modifyPlatformGameBottom(form.getId());
            default:
                return Boolean.FALSE;
        }
    }

    @Override
    public Map<String, List<GameVO>> getGameListByPlatforms(List<String> platformCodeX) {
        return platformCodeX.size() == 0 ? new HashMap<>() : this.lambdaQuery()
                .in(PlatformGame::getPlatformName, platformCodeX)
                .list()
                .stream()
                .collect(Collectors.groupingBy(PlatformGame::getPlatformName, Collectors.mapping(game -> toGameVo(game), Collectors.toList())));

    }

    private GameVO toGameVo(PlatformGame game) {
        return GameVO.builder()
                .name(game.getNameEn())
                .id(game.getGameId())
                .build();
    }

}




