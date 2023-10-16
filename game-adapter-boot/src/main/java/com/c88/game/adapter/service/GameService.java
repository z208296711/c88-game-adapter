package com.c88.game.adapter.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.exception.BizException;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.enums.PlatformGameLabelEnum;
import com.c88.game.adapter.enums.TransferRecordStateEnum;
import com.c88.game.adapter.enums.TransferTypeEnum;
import com.c88.game.adapter.event.LaunchGameEvent;
import com.c88.game.adapter.event.model.LaunchGameModel;
import com.c88.game.adapter.mapper.PlatformSortMapper;
import com.c88.game.adapter.mapstruct.H5GameCategoryConverter;
import com.c88.game.adapter.mapstruct.PlatformGameDocumentToPlatformGameLabelConverter;
import com.c88.game.adapter.mapstruct.PlatformGameLabelConverter;
import com.c88.game.adapter.mapstruct.PlatformGameToRepositoryConverter;
import com.c88.game.adapter.pojo.document.PlatformGameDocument;
import com.c88.game.adapter.pojo.entity.*;
import com.c88.game.adapter.pojo.form.ErdTransferForm;
import com.c88.game.adapter.pojo.form.FindKeywordSearchForm;
import com.c88.game.adapter.pojo.form.FindPlatformGameLabelForm;
import com.c88.game.adapter.pojo.vo.H5GameCategoryVO;
import com.c88.game.adapter.pojo.vo.H5PlatformVO;
import com.c88.game.adapter.pojo.vo.PlatformGameLabelVO;
import com.c88.game.adapter.repository.IPlatformGameRepository;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import com.c88.payment.client.MemberBalanceClient;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final IErdGameService iErdGameService;

    private final GameAdapterExecutor gameAdapterExecutor;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final IPlatformService iPlatformService;

    private final IPlatformGameService iPlatformGameService;

    private final IMemberGameSessionService iMemberGameSessionService;

    private final IGameCategoryService iGameCategoryService;

    private final PlatformGameLabelConverter platformGameByLabelConverter;

    private final H5GameCategoryConverter h5GameCategoryConverter;

    private final PlatformGameToRepositoryConverter platformGameToRepositoryConverter;

    private final PlatformGameDocumentToPlatformGameLabelConverter platformGameDocumentToPlatformGameConverter;

    private final IPlatformSortService iPlatformSortService;

    private final IPlatformGameRepository iPlatformGameRepository;

    private final PlatformSortMapper platformSortMapper;

    private final MemberBalanceClient memberBalanceClient;

    private final ElasticsearchOperations operations;

    private final IPlatformGameStyleService iPlatformGameStyleService;

    private final RedisTemplate redisTemplate;

    private final String PLATFORM_GAME = "platform-game";

    private final String PLATFORM_GAME_LABEL_KEY = "#form.labelType + ':' + #form.userDriver + ':' + #form.platformId + ':' + #form.gameCategoryId + ':' + #form.pageNum + ':' + #form.pageSize";

    private final String PLATFORM_GAME_LABEL_CONDITION = "#form.getLabelType() != 1 && #form.getLabelType() != 2";

    @PostConstruct
    public void init() {
        List<PlatformGame> platformGames = iPlatformGameService.list();

        List<PlatformGameDocument> platformGameDocuments = platformGames.stream()
                .map(platformGameToRepositoryConverter::toVo)
                .collect(Collectors.toUnmodifiableList());

        if (iPlatformGameRepository.count() > 0) {
            iPlatformGameRepository.deleteAll();
        }
        iPlatformGameRepository.saveAll(platformGameDocuments);
    }

    @Transactional
    public boolean syncBalanceToPlatform(Long memberId, String username, Platform platform) {
        iErdGameService.createPlatformGameMember(memberId, username, platform);

        //取得會員資訊
        Result<PaymentMemberBalanceDTO> memberBalanceDTOResult = memberBalanceClient.findPaymentMemberBalanceByMemberId(memberId);
        if (!Result.isSuccess(memberBalanceDTOResult)) {
            return false;
        }

        PaymentMemberBalanceDTO memberInfoDTO = memberBalanceDTOResult.getData();
        if (memberInfoDTO.getBalance().compareTo(BigDecimal.ONE) < 0) {
            return false;
        }

        ErdTransferForm erdTransferForm = new ErdTransferForm();
        erdTransferForm.setTransfer(TransferTypeEnum.INTO_THIRD.getValue());
        erdTransferForm.setPlatformCode(platform.getCode());
        erdTransferForm.setAmount(memberInfoDTO.getBalance().setScale(0, RoundingMode.DOWN));
        erdTransferForm.setMemberId(memberInfoDTO.getId());
        iErdGameService.transferOut(memberInfoDTO, erdTransferForm);

        return true;
    }

    /**
     * 取遊戲鏈結
     */
    @Transactional
    public String login(Long memberId,
                        String username,
                        PlatformGame platformGame,
                        HttpServletRequest request
    ) {
        Platform platform = iPlatformService.getPlatformById(platformGame.getPlatformId());
        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(platform.getCode());
        String clientIP = ServletUtil.getClientIP(request);
        //取遊戲鏈結
        Map<String, String> param = new HashMap<>();
        param.put("GameId", platformGame.getGameId());
        param.put("ip", clientIP);
        Result<String> result = currentGameAdapter.login(username, param);
        if (Result.isSuccess(result)) {

            // 成功增加一些紀錄操作, 異步操作  ex: 開啟紀錄
            LaunchGameEvent launchGameEvent = new LaunchGameEvent(
                    LaunchGameModel.builder()
                            .memberId(memberId.intValue())
                            .username(username)
                            .platformGameId(platformGame.getId())
                            .platformCode(platform.getName())
                            .ip(clientIP)
                            .build()
            );
            applicationEventPublisher.publishEvent(launchGameEvent);
            return result.getData();
        }
        return "";
    }

    public MemberGameSession getLastMemberGameSession(Long uid) {
        return iMemberGameSessionService.getOne(new LambdaQueryWrapper<MemberGameSession>()
                .eq(MemberGameSession::getMemberId, uid)
                .orderByDesc(MemberGameSession::getGmtCreate).last("limit 1"));
    }

    /**
     * 取所有主錢包+三方餘額並存入會員錢包
     */
    @Transactional
    public BigDecimal findAllBalance(Long memberId) {
        List<Platform> platforms = iPlatformService.lambdaQuery()
                .eq(Platform::getEnable, EnableEnum.START.getCode())
                .list();
        List<PlatformGameMember> platformGameMemberList = iPlatformGameMemberService.lambdaQuery()
                .eq(PlatformGameMember::getMemberId, memberId)
                .in(PlatformGameMember::getPlatformId, platforms.stream().filter(filter -> filter.getCanTransferOut() == 1).map(Platform::getId).collect(Collectors.toList()))
                .list();

        // 取得錢包的餘額
        Result<PaymentMemberBalanceDTO> paymentMemberBalanceResult = memberBalanceClient.findPaymentMemberBalanceByMemberId(memberId);
        if (!Result.isSuccess(paymentMemberBalanceResult)) {
            throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        PaymentMemberBalanceDTO memberBalanceDTO = paymentMemberBalanceResult.getData();

        // 取得遊戲平台餘額
        BigDecimal allBalance = this.allPlatformTransferIn(memberBalanceDTO, platformGameMemberList);

        BigDecimal newAmount = memberBalanceDTO.getBalance().add(allBalance);
        // 有餘額時轉回會員錢包
        return newAmount;
    }

    private BigDecimal allPlatformTransferIn(PaymentMemberBalanceDTO memberBalanceDTO, List<PlatformGameMember> platformGameMemberList) {
        return platformGameMemberList
                .stream()
                .map(platformGameMember -> {
                            BigDecimal transferAmount;
                            String key = RedisUtils.buildKey(RedisKey.GAME_TRANSFER_TO_PLATFORM, platformGameMember.getId());
                            Object o = redisTemplate.opsForValue().get(key);
                            if (Objects.isNull(o)) {
                                redisTemplate.opsForValue().set(key, true);
                                String platformCode = platformGameMember.getCode();
                                transferAmount = iErdGameService.balance(memberBalanceDTO, platformCode);
                                if (transferAmount != null && transferAmount.compareTo(BigDecimal.ZERO) > 0) {
                                    ErdTransferForm erdTransferForm = new ErdTransferForm();
                                    erdTransferForm.setAmount(transferAmount);
                                    erdTransferForm.setTransfer(TransferTypeEnum.INTO_PLATFORM.getValue());
                                    erdTransferForm.setPlatformCode(platformCode);
                                    TransferRecordStateEnum transferStateEnum = iErdGameService.transferIntoPlatform(memberBalanceDTO, erdTransferForm);
                                    if (!transferStateEnum.equals(TransferRecordStateEnum.SUCCESS)) {
                                        transferAmount = BigDecimal.ZERO;
                                    }
                                } else {
                                    transferAmount = BigDecimal.ZERO;
                                }
                                redisTemplate.delete(key);
                            } else {
                                transferAmount = BigDecimal.ZERO;
                            }
                            return transferAmount;
                        }
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Cacheable(cacheNames = RedisKey.PLATFORM_GAME_LABEL,
            key = PLATFORM_GAME_LABEL_KEY,
            condition = PLATFORM_GAME_LABEL_CONDITION)
    public IPage<PlatformGameLabelVO> findPlatformGameLabel(Long memberId, FindPlatformGameLabelForm form) {
        switch (PlatformGameLabelEnum.getEnum(form.getLabelType())) {
            case ALL:
                return findAllGame(form);
            case HOT:
                return findHotGame(form);
            case RECENT:
                return findRecentGame(memberId, form);
            case RECOMMEND_BANNER:
                return findRecommendBanner(form);
            default:
                return null;
        }
    }

    /**
     * 最多抓取10筆資料
     * 優先採後台手動設定
     * 如未滿10筆
     * 其餘筆數採自動抓取近期30天投注量最多的遊戲。
     */
    private IPage<PlatformGameLabelVO> findHotGame(FindPlatformGameLabelForm form) {
        IPage<PlatformGameLabelVO> platformGamePage = iPlatformGameService.lambdaQuery()
                .eq(Objects.nonNull(form.getPlatformId()), PlatformGame::getPlatformId, form.getPlatformId())
                .eq(Objects.nonNull(form.getGameCategoryId()), PlatformGame::getGameCategoryId, form.getGameCategoryId())
                .eq(form.getUserDriver() == 0, PlatformGame::getPcDeviceVisible, 1)
                .eq(form.getUserDriver() == 1, PlatformGame::getH5DeviceVisible, 1)
                .eq(PlatformGame::getHotFlag, 1)
                .eq(PlatformGame::getEnable, 1)
                .orderByAsc(PlatformGame::getGameSort)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformGameByLabelConverter::toVo);

        // 未滿十筆取得近期三十天投注量最多的遊戲
        if (platformGamePage.getSize() < 10) {
            //todo 近期三十天投注量最多的遊戲
        }

        return platformGamePage;
    }

    /**
     * 最多抓取10筆資料
     * 顯示紀錄用戶近期玩過的遊戲。
     */
    private IPage<PlatformGameLabelVO> findRecentGame(Long memberId, FindPlatformGameLabelForm form) {
        Set<Integer> ids = iMemberGameSessionService.lambdaQuery()
                .eq(MemberGameSession::getMemberId, memberId)
                .last("limit 10")
                .orderByDesc(MemberGameSession::getId)
                .list()
                .stream()
                .map(MemberGameSession::getPlatformGameId)
                .collect(Collectors.toUnmodifiableSet());

        if (CollectionUtils.isEmpty(ids)) {
            return new Page<>(form.getPageNum(), form.getPageSize());
        }

        return iPlatformGameService.lambdaQuery()
                .in(CollectionUtils.isNotEmpty(ids), PlatformGame::getId, ids)
                .eq(Objects.nonNull(form.getPlatformId()), PlatformGame::getPlatformId, form.getPlatformId())
                .eq(Objects.nonNull(form.getGameCategoryId()), PlatformGame::getGameCategoryId, form.getGameCategoryId())
                .eq(form.getUserDriver() == 0, PlatformGame::getPcDeviceVisible, 1)
                .eq(form.getUserDriver() == 1, PlatformGame::getH5DeviceVisible, 1)
                .eq(PlatformGame::getEnable, 1)
                .orderByAsc(PlatformGame::getGameSort)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformGameByLabelConverter::toVo);
    }

    private IPage<PlatformGameLabelVO> findAllGame(FindPlatformGameLabelForm form) {
        return iPlatformGameService.lambdaQuery()
                .eq(Objects.nonNull(form.getPlatformId()), PlatformGame::getPlatformId, form.getPlatformId())
                .eq(Objects.nonNull(form.getGameCategoryId()), PlatformGame::getGameCategoryId, form.getGameCategoryId())
                .eq(form.getUserDriver() == 0, PlatformGame::getPcDeviceVisible, 1)
                .eq(form.getUserDriver() == 1, PlatformGame::getH5DeviceVisible, 1)
                .eq(PlatformGame::getEnable, 1)
                .orderByAsc(PlatformGame::getGameSort)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformGameByLabelConverter::toVo);
    }

    @Cacheable(cacheNames = RedisKey.GAME_CATEGORY_PLATFORM, key = "'all'")
    public List<H5GameCategoryVO> findGameCategoryPlatform() {
        //取遊戲類型

        Map<String, Integer> gameStyleMap = iPlatformGameStyleService.list()
                .stream()
                .collect(Collectors.toMap(PlatformGameStyle::getGameCategoryCode, PlatformGameStyle::getStyleType));

        List<GameCategory> gameCategory = new ArrayList<>();

        gameCategory.add(
                GameCategory.builder()
                        .id(0)
                        .name("hot")
                        .code("hot")
                        .toGameLobby(-1)
                        .sort(0)
                        .build()
        );

        gameCategory.addAll(
                iGameCategoryService.lambdaQuery()
                        .orderByAsc(GameCategory::getSort)
                        .list()
        );

        List<H5PlatformVO> platforms = platformSortMapper.findAllPlatformGameByGameCategory();
        List<H5PlatformVO> platformsByHot = platformSortMapper.findAllHotPlatformGameByGameCategory();

        return gameCategory.stream()
                .map(category ->
                        {
                            H5GameCategoryVO vo = h5GameCategoryConverter.toVo(category);

                            vo.setStyleType(gameStyleMap.get(category.getCode()));
                            if (category.getId() == 0) {
                                vo.setPlatforms(platformsByHot);
                            } else {
                                vo.setPlatforms(
                                        platforms.stream()
                                                .filter(filter -> filter.getGameCategoryId().intValue() == category.getId())
                                                .sorted(Comparator.comparing(H5PlatformVO::getSort))
                                                .collect(Collectors.toList())
                                );
                            }
                            return vo;
                        }
                )
                .filter(filter -> CollectionUtils.isNotEmpty(filter.getPlatforms()))
                .collect(Collectors.toList());
    }

    public Set<String> findCompleteSearch(String q) {
        WildcardQueryBuilder queryBuilder = QueryBuilders.wildcardQuery("name", "*" + q + "*");

        Query query = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .build();

        SearchHits<PlatformGameDocument> search =
                operations.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        return search.stream().map(x -> x.getContent().getNameVi()).collect(Collectors.toUnmodifiableSet());
    }

    public IPage<PlatformGameLabelVO> findKeywordSearch(FindKeywordSearchForm form) {
        BoolQueryBuilder boolQueryBuilderMust = QueryBuilders.boolQuery();
        BoolQueryBuilder boolQueryBuilderShould = QueryBuilders.boolQuery();

        boolQueryBuilderShould.should(QueryBuilders.wildcardQuery("nameVi", "*" + form.getKeyword() + "*"))
                .should(QueryBuilders.wildcardQuery("nameEn", "*" + form.getKeyword() + "*"))
                .should(QueryBuilders.matchQuery("tags", form.getKeyword()));

        boolQueryBuilderMust.must(QueryBuilders.matchQuery("enable", "1"))
                .must(boolQueryBuilderShould);

        Query query = new NativeSearchQueryBuilder()
                .withFilter(boolQueryBuilderMust)
                .withQuery(QueryBuilders.matchQuery(form.getUserDriver() == 0 ? "pcDeviceVisible" : "h5DeviceVisible", "1"))
                .withPageable(PageRequest.of(form.getPageNum() - 1, form.getPageSize()))
                .build();

        SearchHits<PlatformGameDocument> search =
                operations.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        List<PlatformGameLabelVO> platformGameLabels = search.stream()
                .map(document -> platformGameDocumentToPlatformGameConverter.toVo(document.getContent()))
                .collect(Collectors.toUnmodifiableList());

        return new Page<PlatformGameLabelVO>()
                .setRecords(platformGameLabels)
                .setCurrent(form.getPageNum())
                .setTotal(search.getTotalHits())
                .setSize(form.getPageSize());
    }

    public IPage<PlatformGameLabelVO> findRecommendBanner(FindPlatformGameLabelForm form) {
        return iPlatformGameService.lambdaQuery()
                .eq(PlatformGame::getRecommendFlag, 1)
                .eq(Objects.nonNull(form.getPlatformId()), PlatformGame::getPlatformId, form.getPlatformId())
                .eq(Objects.nonNull(form.getGameCategoryId()), PlatformGame::getGameCategoryId, form.getGameCategoryId())
                .eq(form.getUserDriver() == 0, PlatformGame::getPcDeviceVisible, 1)
                .eq(form.getUserDriver() == 1, PlatformGame::getH5DeviceVisible, 1)
                .eq(PlatformGame::getEnable, 1)
                .orderByAsc(PlatformGame::getGameSort)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformGameByLabelConverter::toVo);
    }

}

