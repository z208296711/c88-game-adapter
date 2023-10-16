package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.core.base.BasePageQuery;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.MemberUtils;
import com.c88.common.xxljob.dto.UpdateXxlJobDto;
import com.c88.common.xxljob.service.XxlJobService;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.dto.PlatformDTO;
import com.c88.game.adapter.enums.PlatformScheduleMaintainStateEnum;
import com.c88.game.adapter.enums.PlatformScheduleTypeEnum;
import com.c88.game.adapter.enums.SortTypeEnum;
import com.c88.game.adapter.mapper.PlatformMapper;
import com.c88.game.adapter.mapper.PlatformSortMapper;
import com.c88.game.adapter.mapstruct.PlatformConverter;
import com.c88.game.adapter.pojo.entity.GameCategory;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformSort;
import com.c88.game.adapter.pojo.form.ModifyPlatformForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformGameSortForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortHotGameForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortTopBottomForm;
import com.c88.game.adapter.pojo.query.PlatformSortQuery;
import com.c88.game.adapter.pojo.vo.PlatformHotGameVO;
import com.c88.game.adapter.pojo.vo.PlatformRateVO;
import com.c88.game.adapter.pojo.vo.PlatformSortVO;
import com.c88.game.adapter.pojo.vo.PlatformVO;
import com.c88.game.adapter.service.IGameCategoryService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.IPlatformSortService;
import com.c88.member.vo.OptionVO;
import com.c88.storage.service.GCPFileUploadService;
import com.google.cloud.storage.Blob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, Platform> implements IPlatformService {

    private final GCPFileUploadService gcpFileUploadService;

    private final IGameCategoryService iGameCategoryService;

    private final IPlatformSortService iPlatformSortService;

    private final PlatformConverter platformConverter;

    private final PlatformSortMapper platformSortMapper;

    private final XxlJobService xxlJobService;

    public IPage<PlatformVO> findPlatform(BasePageQuery form) {
        IPage<PlatformVO> page = this.page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformConverter::toVo);

        List<Long> platformId = page.getRecords().stream().map(PlatformVO::getId).collect(Collectors.toList());

        List<PlatformSort> platformSorts = iPlatformSortService.lambdaQuery()
                .in(PlatformSort::getPlatformId, platformId)
                .list();

        Map<Integer, String> gameCategoryMap = iGameCategoryService.lambdaQuery()
                .select(GameCategory::getId, GameCategory::getCode)
                .list()
                .stream()
                .collect(Collectors.toMap(GameCategory::getId, GameCategory::getCode));

        page.getRecords()
                .forEach(platformVO ->
                        platformVO.setPlatformHotGames(
                                platformSorts.stream()
                                        .filter(filter -> filter.getPlatformId() == platformVO.getId().intValue())
                                        .map(x -> PlatformHotGameVO.builder()
                                                .gameCategoryId(x.getGameCategoryId())
                                                .gameCategoryCode(gameCategoryMap.get(x.getGameCategoryId()))
                                                .isHot(x.getHot())
                                                .build()
                                        )
                                        .collect(Collectors.toList())
                        )
                );

        return page;

    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_BY_ID, allEntries = true)
    })
    @Override
    public Boolean modifyPlatform(ModifyPlatformForm form) {
        Platform platform = Optional.ofNullable(this.getById(form.getId()))
                .orElseThrow(() -> new BizException(ResultCode.PARAM_ERROR));

        // 排程類型不為空時更新任務中心
        if (Objects.nonNull(form.getScheduleType())) {
            UpdateXxlJobDto xxlJobDto = UpdateXxlJobDto.builder()
                    .jobGroup(2)
                    .author(MemberUtils.getUsername())
                    .scheduleType("CRON")
                    .executorRouteStrategy("RANDOM")
                    .misfireStrategy("DO_NOTHING")
                    .executorBlockStrategy("SERIAL_EXECUTION")
                    .build();

            // 取得排程的開始及結束時間
            LocalDateTime scheduleStartTime = form.getScheduleStartTime();
            LocalDateTime scheduleEndTime = form.getScheduleEndTime();
            if (Objects.isNull(scheduleStartTime) || Objects.isNull(scheduleEndTime)) {
                throw new BizException(ResultCode.PLATFORM_MAINTAIN_TASK_NOT_SETTING);
            }

            LocalDateTime now = LocalDateTime.now();

            // 任務中心調用前綴
            String handlerStartPrefix = "maintainStartBy";
            String handlerEndPrefix = "maintainEndBy";

            // 任務中心註記後綴
            String jobDescStartSuffix = "維護啟用任務";
            String jobDescEndSuffix = "維護停用任務";

            // 判斷維護時間在範圍內直接改為維護中
            if (scheduleStartTime.isBefore(now) && scheduleEndTime.isAfter(now)) {
                form.setMaintainState(PlatformScheduleMaintainStateEnum.MAINTAIN_STATE_START.getCode());
            }

            switch (PlatformScheduleTypeEnum.getEnum(form.getScheduleType())) {
                case NOT:
                    break;
                case DAY:
                    // 更新啟動維護時間排程
                    xxlJobDto.setId(platform.getScheduleStartId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescStartSuffix);
                    xxlJobDto.setExecutorHandler(handlerStartPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleStartTime.format(DateTimeFormatter.ofPattern("0 mm HH * * ?")));
                    xxlJobService.updateJob(xxlJobDto);

                    // 更新停用維護時間排程
                    xxlJobDto.setId(platform.getScheduleEndId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescEndSuffix);
                    xxlJobDto.setExecutorHandler(handlerEndPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleEndTime.format(DateTimeFormatter.ofPattern("0 mm HH * * ?")));
                    xxlJobService.updateJob(xxlJobDto);
                    break;
                case WEEK:
                    // 更新啟動維護時間排程
                    xxlJobDto.setId(platform.getScheduleStartId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescStartSuffix);
                    xxlJobDto.setExecutorHandler(handlerStartPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleStartTime.format(DateTimeFormatter.ofPattern("0 mm HH ? * ")) + form.getScheduleWeek());
                    xxlJobService.updateJob(xxlJobDto);

                    // 更新停用維護時間排程
                    xxlJobDto.setId(platform.getScheduleEndId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescEndSuffix);
                    xxlJobDto.setExecutorHandler(handlerEndPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleEndTime.format(DateTimeFormatter.ofPattern("0 mm HH ? * ")) + form.getScheduleWeek());
                    xxlJobService.updateJob(xxlJobDto);
                    break;
                case MONTH:
                    // 更新啟動維護時間排程
                    xxlJobDto.setId(platform.getScheduleStartId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescStartSuffix);
                    xxlJobDto.setExecutorHandler(handlerStartPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleStartTime.format(DateTimeFormatter.ofPattern("0 mm HH * ")) + form.getScheduleMonth() + " ?");
                    xxlJobService.updateJob(xxlJobDto);

                    // 更新停用維護時間排程
                    xxlJobDto.setId(platform.getScheduleEndId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescEndSuffix);
                    xxlJobDto.setExecutorHandler(handlerEndPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleEndTime.format(DateTimeFormatter.ofPattern("0 mm HH * ")) + form.getScheduleMonth() + " ?");
                    xxlJobService.updateJob(xxlJobDto);
                    break;
                case SCOPE:
                    // 更新啟動維護時間排程
                    xxlJobDto.setId(platform.getScheduleStartId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescStartSuffix);
                    xxlJobDto.setExecutorHandler(handlerStartPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleStartTime.format(DateTimeFormatter.ofPattern("ss mm HH dd MM ? yyyy")));
                    xxlJobService.updateJob(xxlJobDto);

                    // 更新停用維護時間排程
                    xxlJobDto.setId(platform.getScheduleEndId());
                    xxlJobDto.setJobDesc(platform.getCode() + jobDescEndSuffix);
                    xxlJobDto.setExecutorHandler(handlerEndPrefix + platform.getCode());
                    xxlJobDto.setScheduleConf(scheduleEndTime.format(DateTimeFormatter.ofPattern("ss mm HH dd MM ? yyyy")));
                    xxlJobService.updateJob(xxlJobDto);
                    break;
                default:
                    throw new BizException(ResultCode.RESOURCE_NOT_FOUND);
            }
        }

        // 例行維護開關 有修改時連同任務中心控制開關
        if (Objects.nonNull(form.getScheduleMaintainState())) {
            PlatformScheduleMaintainStateEnum stateEnum = PlatformScheduleMaintainStateEnum.getEnum(form.getScheduleMaintainState());
            if (stateEnum == PlatformScheduleMaintainStateEnum.MAINTAIN_STATE_START) {
                xxlJobService.startJob(platform.getScheduleStartId());
                xxlJobService.startJob(platform.getScheduleEndId());
            } else {
                xxlJobService.stopJob(platform.getScheduleStartId());
                xxlJobService.stopJob(platform.getScheduleEndId());
            }
        }

        return this.updateById(
                Platform.builder()
                        .id(form.getId())
                        .name(form.getName())
                        .canTransferIn(form.getCanTransferIn())
                        .canTransferOut(form.getCanTransferOut())
                        .maintainState(form.getMaintainState())
                        .scheduleMaintainState(form.getScheduleMaintainState())
                        .scheduleType(form.getScheduleType())
                        .scheduleWeek(form.getScheduleWeek())
                        .scheduleMonth(form.getScheduleMonth())
                        .scheduleStartTime(form.getScheduleStartTime())
                        .scheduleEndTime(form.getScheduleEndTime())
                        .enable(form.getEnable())
                        .build()
        );

    }

    /**
     * 取得平台
     *
     * @return 平台
     */
    @Cacheable(cacheNames = RedisKey.PLATFORM_BY_ID, key = "#id")
    public Platform getPlatformById(Long id) {
        return this.lambdaQuery().eq(Platform::getId, id)
                .oneOpt()
                .orElseThrow(() -> new BizException("無平台"));
    }

    @Override
    public String uploadImage(MultipartFile file) {
        Blob blob = gcpFileUploadService.upload(file, "paymentTest", true);
        return blob.getName();
    }

    @Override
    public List<PlatformSortVO> findPlatformSort(Integer gameCategoryId) {
        return this.baseMapper.findPlatformSort(gameCategoryId);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true)
    })
    @Transactional
    @Override
    public Boolean modifyPlatformSort(ModifyPlatformSortForm form) {

        //遊戲類型查尋判斷
        Optional<GameCategory> gameCategoryOpt = Optional.ofNullable(iGameCategoryService.getById(form.getGameCategoryId()));
        gameCategoryOpt.orElseThrow(() -> new BizException(ResultCode.PARAM_ERROR));

        //遊戲平台排序查找
        List<Integer> platformIdList = form.getPlatformSortQueries().stream().map(PlatformSortQuery::getPlatformId).collect(Collectors.toList());
        List<PlatformSort> platformSortList = iPlatformSortService.lambdaQuery().in(PlatformSort::getPlatformId, platformIdList).list();

        //更新排序
        List<PlatformSort> modifiedPlatformSortList = form.getPlatformSortQueries().stream().map(platformSortQuery -> {
            PlatformSort platformSort = platformSortList.stream()
                    .filter(filter -> filter.getPlatformId().equals(platformSortQuery.getPlatformId()))
                    .filter(filter -> filter.getGameCategoryId().equals(form.getGameCategoryId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ResultCode.PARAM_ERROR));
            platformSort.setSort(platformSortQuery.getSort());
            return platformSort;
        }).collect(Collectors.toList());

        return iPlatformSortService.updateBatchById(modifiedPlatformSortList);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.PLATFORM_GAME_LABEL, allEntries = true)
    })
    @Override
    public Boolean modifyPlatformSortTopBottom(ModifyPlatformSortTopBottomForm form) {
        SortTypeEnum sortType = SortTypeEnum.getEnum(form.getSortType());
        switch (sortType) {
            case TOP:
                return platformSortMapper.modifyPlatformSortTop(form.getPlatformSortId());
            case BOTTOM:
                return platformSortMapper.modifyPlatformSortBottom(form.getPlatformSortId());
            default:
                return Boolean.FALSE;
        }
    }

    @Override
    public List<OptionVO<Long>> findPlatformOption() {
        return this.lambdaQuery()
                .select(Platform::getId, Platform::getName)
                .list()
                .stream()
                .map(platform -> OptionVO.<Long>builder()
                        .value(platform.getId())
                        .label(platform.getName())
                        .build()
                )
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<OptionVO<String>> findPlatformCodeOption() {
        return this.lambdaQuery()
                .select(Platform::getCode, Platform::getName)
                .list()
                .stream()
                .map(platform -> OptionVO.<String>builder()
                        .value(platform.getCode())
                        .label(platform.getName())
                        .build()
                )
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<PlatformRateVO> findPlatformRate() {
        return this.lambdaQuery().select(Platform::getId, Platform::getName, Platform::getRate)
                .list()
                .stream()
                .map(platformConverter::toPlatformRateVO)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean modifyPlatformRate(Map<Long, BigDecimal> map) {
        return this.updateBatchById(
                map.entrySet()
                        .parallelStream()
                        .map(x -> Platform.builder().id(x.getKey()).rate(x.getValue().divide(new BigDecimal("100"), 5, RoundingMode.HALF_DOWN)).build())
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<PlatformDTO> findAllPlatformDTO() {
        return this.lambdaQuery()
                .list()
                .stream()
                .map(platformConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformSortVO> findPlatformSortByHot() {
        return this.baseMapper.findPlatformSortByHot();
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true)
    })
    @Override
    public Boolean modifyPlatformSortByHot(ModifyPlatformGameSortForm form) {
        return iPlatformSortService.updateBatchById(
                form.getGameAdapterSortRequests()
                        .stream()
                        .map(x ->
                                PlatformSort.builder()
                                        .id(x.getId())
                                        .hotSort(x.getSort())
                                        .build()
                        )
                        .collect(Collectors.toUnmodifiableList())
        );
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true)
    })
    @Override
    public Boolean modifyPlatformSortTopBottomByHot(ModifyPlatformSortTopBottomForm form) {
        SortTypeEnum sortType = SortTypeEnum.getEnum(form.getSortType());
        switch (sortType) {
            case TOP:
                return platformSortMapper.modifyPlatformSortTopByHot(form.getPlatformSortId());
            case BOTTOM:
                return platformSortMapper.modifyPlatformSortBottomByHot(form.getPlatformSortId());
            default:
                return Boolean.FALSE;
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, allEntries = true)
    })
    public Boolean modifyPlatformSortHotGame(ModifyPlatformSortHotGameForm form) {
        // 重置熱門遊戲
        iPlatformSortService.lambdaUpdate()
                .eq(PlatformSort::getPlatformId, form.getPlatformId())
                .set(PlatformSort::getHot, 0)
                .update();

        if (CollectionUtils.isNotEmpty(form.getGameCategoryId())) {
            // 設定熱門遊戲
            iPlatformSortService.lambdaUpdate()
                    .eq(PlatformSort::getPlatformId, form.getPlatformId())
                    .in(PlatformSort::getGameCategoryId, form.getGameCategoryId())
                    .set(PlatformSort::getHot, 1)
                    .update();
        }

        return Boolean.TRUE;
    }

}
