package com.c88.game.adapter.controller.admin;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.affiliate.api.dto.AffiliateMemberDTO;
import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.pojo.document.BetOrderDocument;
import com.c88.game.adapter.pojo.entity.*;
import com.c88.game.adapter.pojo.form.BetRebateRecordForm;
import com.c88.game.adapter.pojo.form.FindMemberGameWinLossForm;
import com.c88.game.adapter.pojo.vo.GameCategoryTypeDetailReportVO;
import com.c88.game.adapter.pojo.vo.GameCategoryTypeReportVO;
import com.c88.game.adapter.pojo.vo.GameWinLossReportVO;
import com.c88.game.adapter.pojo.vo.MemberRebateRecordTotalVO;
import com.c88.game.adapter.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jodd.util.CollectionUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Tag(name = "『後台』報表")
@RequiredArgsConstructor
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ElasticsearchOperations operations;

    private final IPlatformService iPlatformService;

    private final IPlatformSortService iPlatformSortService;

    private final IGameCategoryService iGameCategoryService;

    private final IMemberRebateRecordService memberRebateRecordService;

    private final AffiliateMemberClient affiliateMemberClient;

    private final ICategoryRebateRecordService categoryRebateRecordService;

    @Operation(summary = "遊戲輸贏報表")
    @GetMapping("/game/win/loss")
    public Result<GameWinLossReportVO> findMemberGameWinLoss(@ParameterObject FindMemberGameWinLossForm form) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // 時間類型 1投注時間 2結算時間
        Map<Integer, String> timeTypeMap = Map.of(
                1, "transactionTime",
                2, "settleTime"
        );

        // 找注單
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withFilter(
                        QueryBuilders.rangeQuery(timeTypeMap.getOrDefault(form.getTimeType(), "settleTime"))
                                .gt(form.getStartTime().format(dtf))
                                .lte(form.getEndTime().format(dtf)))
                .build();

        SearchHits<BetOrderDocument> search = operations.search(searchQuery, BetOrderDocument.class);

        // 遊戲類型
        Map<Integer, String> gameCategoryMap = iGameCategoryService.list()
                .stream()
                .collect(Collectors.toMap(GameCategory::getId, GameCategory::getCode));

        Map<String, List<BetOrderDocument>> betsByGameCategoryCode = search.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(BetOrderDocument::getGameCategoryCode));

        Map<Integer, List<PlatformSort>> platformByGameCategoryId = iPlatformSortService.list()
                .stream()
                .collect(Collectors.groupingBy(PlatformSort::getGameCategoryId));

        Map<Integer, String> platformNameByIdMap = iPlatformService.list()
                .stream()
                .collect(Collectors.toMap(platform -> platform.getId().intValue(), Platform::getName));

        Map<String, BigDecimal> rebateMap = categoryRebateRecordService.lambdaQuery()
                .list().stream().collect(Collectors.groupingBy(CategoryRebateRecord::getGameCategoryCode,
                        Collectors.reducing(BigDecimal.ZERO, CategoryRebateRecord::getRebate, BigDecimal::add)));

        // 組合報表
        List<GameCategoryTypeReportVO> gameCategoryTypes = platformByGameCategoryId.entrySet()
                .stream()
                .map(gameCategory -> {
                            List<GameCategoryTypeDetailReportVO> details = gameCategory.getValue()
                                    .stream()
                                    .map(byPlatform -> {
                                                List<BetOrderDocument> betOrderDocuments = betsByGameCategoryCode.getOrDefault(gameCategoryMap.get(byPlatform.getGameCategoryId()), Collections.emptyList());
                                                List<BetOrderDocument> betOrders = betOrderDocuments.stream()
                                                        .filter(filter -> filter.getPlatformId().intValue() == byPlatform.getPlatformId() && filter.getBetState() == 1)
                                                        .collect(Collectors.toList());

                                                return GameCategoryTypeDetailReportVO.builder()
                                                        .gameCategoryName(platformNameByIdMap.get(byPlatform.getPlatformId()) + ":" + gameCategoryMap.get(gameCategory.getKey()))
                                                        .betManCount(betOrders.stream().map(BetOrderDocument::getMemberId).distinct().count())
                                                        .betCount((long) betOrders.size())
                                                        .validBetAmount(betOrders.stream().map(BetOrderDocument::getValidBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                                                        .winLoss(betOrders.stream().map(BetOrderDocument::getWinLoss).reduce(BigDecimal.ZERO, BigDecimal::add).negate())
                                                        .rebate(BigDecimal.ZERO)
                                                        .build();
                                            }
                                    )
                                    .collect(Collectors.toList());

                            return GameCategoryTypeReportVO.builder()
                                    .gameCategoryName(gameCategoryMap.get(gameCategory.getKey()))
                                    .gameCategoryTypeDetails(details)
                                    .betManCount(details.stream().mapToLong(GameCategoryTypeDetailReportVO::getBetManCount).sum())
                                    .betCount(details.stream().mapToLong(GameCategoryTypeDetailReportVO::getBetCount).sum())
                                    .validBetAmount(details.stream().map(GameCategoryTypeDetailReportVO::getValidBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                                    .winLoss(details.stream().map(GameCategoryTypeDetailReportVO::getWinLoss).reduce(BigDecimal.ZERO, BigDecimal::add))
                                    .rebate(Objects.isNull(rebateMap.get(String.valueOf(gameCategory.getKey())))?BigDecimal.ZERO:rebateMap.get(String.valueOf(gameCategory.getKey())))
                                    .build();
                        }
                )
                .collect(Collectors.toList());




        return Result.success(
                GameWinLossReportVO.builder()
                        .gameCategoryTypes(gameCategoryTypes)
                        .totalBetManCount(gameCategoryTypes.stream().mapToLong(GameCategoryTypeReportVO::getBetManCount).sum())
                        .totalBetCount(gameCategoryTypes.stream().mapToLong(GameCategoryTypeReportVO::getBetCount).sum())
                        .totalValidBetAmount(gameCategoryTypes.stream().map(GameCategoryTypeReportVO::getValidBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                        .totalWinLoss(gameCategoryTypes.stream().map(GameCategoryTypeReportVO::getWinLoss).reduce(BigDecimal.ZERO, BigDecimal::add))
                        .totalRebate(gameCategoryTypes.stream().map(GameCategoryTypeReportVO::getRebate).reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build()
        );
    }


    @Operation(summary = "返水報表")
    @GetMapping("/rebate")
    public PageResult<MemberRebateRecord> findRebateRecord(@ParameterObject BetRebateRecordForm form) {
        Page<MemberRebateRecord> page = memberRebateRecordService.lambdaQuery()
                .ge(Objects.nonNull(form.getStartTime()), MemberRebateRecord::getGmtCreate, form.getStartTime())
                .le(Objects.nonNull(form.getEndTime()), MemberRebateRecord::getGmtCreate, form.getEndTime())
                .eq(StringUtils.isNotBlank(form.getUsername()), MemberRebateRecord::getUsername, form.getUsername())
                .page(new Page<>(form.getPageNum(), form.getPageSize()));
        List<MemberRebateRecord> memberRebateRecords = memberRebateRecordService.lambdaQuery()
                .ge(Objects.nonNull(form.getStartTime()), MemberRebateRecord::getGmtCreate, form.getStartTime())
                .le(Objects.nonNull(form.getEndTime()), MemberRebateRecord::getGmtCreate, form.getEndTime())
                .eq(StringUtils.isNotBlank(form.getUsername()), MemberRebateRecord::getUsername, form.getUsername()).list();
        MemberRebateRecordTotalVO memberRebateRecordTotalVO = memberRebateRecords.stream()
                .map(JSON::toJSONString)
                .map(o -> JSON.parseObject(o,MemberRebateRecordTotalVO.class))
                .reduce((i, n) ->
                        MemberRebateRecordTotalVO.builder().betTimes(i.getBetTimes() + n.getBetTimes())
                                .betAmount(i.getBetAmount().add(n.getBetAmount()))
                                .settle(i.getSettle().add(n.getSettle()))
                                .validBetAmount(i.getValidBetAmount().add(n.getValidBetAmount()))
                                .rebate(i.getRebate().add(n.getRebate())).build()
                ).orElse(new MemberRebateRecordTotalVO(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        if(CollectionUtils.isNotEmpty(page.getRecords())){
            Result<List<AffiliateMemberDTO>> affiliateMembersResult = affiliateMemberClient.findAffiliateMembers(page.getRecords().stream().map(MemberRebateRecord::getMemberId).collect(Collectors.toList()));
            Map<Long, String> affMap = affiliateMembersResult.getData().stream().collect(Collectors.toMap(AffiliateMemberDTO::getMemberId, AffiliateMemberDTO::getParentUsername, ((o, n) -> o)));
            if (Result.isSuccess(affiliateMembersResult)) {
                for (MemberRebateRecord record : page.getRecords()) {
                    record.setParentName(affMap.get(record.getMemberId()));
                }
            }
        }
        return PageResult.success(page,memberRebateRecordTotalVO);
    }

    @Operation(summary = "反水接口", description = "內部調用", hidden = true)
    @GetMapping("/rebate/sum")
    public Result<MemberRebateRecordTotalVO> findRebateRecordSum(@RequestParam() String startTime, @RequestParam() String endTime) {
        List<MemberRebateRecord> memberRebateRecords = memberRebateRecordService.lambdaQuery()
                .ge( MemberRebateRecord::getGmtCreate, startTime)
                .le( MemberRebateRecord::getGmtCreate, endTime)
                .list();
        MemberRebateRecordTotalVO memberRebateRecordTotalVO = memberRebateRecords.stream()
                .map(JSON::toJSONString)
                .map(o -> JSON.parseObject(o,MemberRebateRecordTotalVO.class))
                .reduce((i, n) ->
                        MemberRebateRecordTotalVO.builder().betTimes(i.getBetTimes() + n.getBetTimes())
                                .betAmount(i.getBetAmount().add(n.getBetAmount()))
                                .settle(i.getSettle().add(n.getSettle()))
                                .validBetAmount(i.getValidBetAmount().add(n.getValidBetAmount()))
                                .rebate(i.getRebate().add(n.getRebate())).build()
                ).orElse(new MemberRebateRecordTotalVO(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        return Result.success(memberRebateRecordTotalVO);
    }
    @Operation(summary = "遊戲分類反水接口", description = "內部調用", hidden = true)
    @GetMapping("/rebate/category")
    public Result<List<CategoryRebateRecord>> findRebateRecordCategory(@RequestParam() String startTime, @RequestParam() String endTime) {
        List<CategoryRebateRecord> categoryRebateRecords = categoryRebateRecordService.lambdaQuery()
                .ge(CategoryRebateRecord::getGmtCreate, startTime)
                .le(CategoryRebateRecord::getGmtCreate, endTime)
                .list();
        return Result.success(categoryRebateRecords);
    }


    @Operation(summary = "會員反水接口", description = "內部調用", hidden = true)
    @GetMapping("/rebate/member")
    public Result<List<MemberRebateRecord>> findRebateRecordMember(@RequestParam() String startTime, @RequestParam() String endTime) {
        List<MemberRebateRecord> memberRebateRecords = memberRebateRecordService.lambdaQuery()
                .ge( MemberRebateRecord::getGmtCreate, startTime)
                .le( MemberRebateRecord::getGmtCreate, endTime)
                .list();
        return Result.success(memberRebateRecords);
    }



}
