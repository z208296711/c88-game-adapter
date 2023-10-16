package com.c88.game.adapter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.enums.BetOrderEventTypeEnum;
import com.c88.game.adapter.enums.BetOrderTypeEnum;
import com.c88.game.adapter.event.BetRecord;
import com.c88.game.adapter.mapper.BetOrderMapper;
import com.c88.game.adapter.mapstruct.BetOrderConverter;
import com.c88.game.adapter.pojo.document.BetOrderDocument;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.form.BetAmountRecordForm;
import com.c88.game.adapter.pojo.form.BetOrderRecordDetailForm;
import com.c88.game.adapter.pojo.form.BetOrderRecordForm;
import com.c88.game.adapter.pojo.vo.BetOrderDetailRecordVO;
import com.c88.game.adapter.pojo.vo.BetOrderRecordVO;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import com.c88.game.adapter.pojo.vo.TotalCountVO;
import com.c88.game.adapter.repository.IBetOrderRepository;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.service.IGameCategoryService;
import com.c88.game.adapter.vo.ReportBetOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.c88.game.adapter.enums.BetOrderTypeEnum.BET_STATUS_CONFIRMED;

@Service
@RequiredArgsConstructor
@Slf4j
public class BetOrderServiceImpl extends ServiceImpl<BetOrderMapper, BetOrder> implements IBetOrderService {

    private final IBetOrderRepository<BetOrderDocument> iBetOrderRepository;

    private final BetOrderConverter betOrderConverter;

    private final BetOrderMapper betOrderMapper;

    private final ElasticsearchOperations operations;

    private final IGameCategoryService iGameCategoryService;

    @Override
    @Transactional
    public BetRecord insertOrUpdate(BetOrder betOrder) {
        log.info("allBetOrder:{}", betOrder);
        BetOrder dbBetOrder = this.lambdaQuery()
                .eq(BetOrder::getMemberId, betOrder.getMemberId())
                .eq(BetOrder::getTransactionNo, betOrder.getTransactionNo())
                .one();

        BetRecord betRecord = betOrderConverter.toRecord(betOrder);

        if (dbBetOrder == null) {
            if (Objects.equals(betOrder.getBetState(), BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betRecord.setEventType(BetOrderEventTypeEnum.BET_SETTLED.getValue());
            } else {
                betRecord.setEventType(BetOrderEventTypeEnum.BET_NEW_ORDER.getValue());
            }
            this.save(betOrder);
        } else {
            //判斷訂單狀態是否都相同
            if (dbBetOrder.getBetState().equals(betOrder.getBetState()) &&
                    dbBetOrder.getBetAmount().compareTo(betOrder.getBetAmount()) == 0 &&
                    dbBetOrder.getSettle().compareTo(betOrder.getSettle()) == 0) {
                betRecord.setEventType(BetOrderEventTypeEnum.BET_ALREADY_PULL.getValue());
            } else if (Objects.equals(dbBetOrder.getBetState(), BET_STATUS_CONFIRMED.getValue()) &&
                    Objects.equals(betOrder.getBetState(), BetOrderTypeEnum.BET_STATUS_SETTLED.getValue())) {
                betRecord.setEventType(BetOrderEventTypeEnum.BET_SETTLED.getValue());
            } else if (Objects.equals(dbBetOrder.getBetState(), BetOrderTypeEnum.BET_STATUS_SETTLED.getValue()) &&
                    Objects.equals(betOrder.getBetState(), BetOrderTypeEnum.BET_STATUS_CANCELED.getValue())) {
                betRecord.setEventType(BetOrderEventTypeEnum.BET_CANCELED.getValue());
            } else {
                betRecord.setEventType(BetOrderEventTypeEnum.BET_UPDATE_SETTLE.getValue());
                betRecord.setSettleDiff(betOrder.getSettle().subtract(dbBetOrder.getSettle()));
            }
            betOrder.setId(dbBetOrder.getId());
            betOrder.setMemberId(null);
            this.updateById(betOrder);
        }
        log.info("show betRecord:{}", JSON.toJSONString(betRecord));
        BetOrderDocument betOrderDocument = betOrderConverter.toDocument(betOrder);

        iBetOrderRepository.save(betOrderDocument);
        return betRecord;
    }

    @Override
    public BigDecimal findMemberNonSettleBetAmount(Long uid) {
        BetOrder betOrder = getOne(new QueryWrapper<BetOrder>()
                .select("sum(valid_bet_amount)as valid_bet_amount")
                .lambda()
                .eq(BetOrder::getMemberId, uid)
                .eq(BetOrder::getBetState, BET_STATUS_CONFIRMED.getValue())
                .groupBy(BetOrder::getMemberId));
        return betOrder != null ? betOrder.getValidBetAmount() : BigDecimal.ZERO;
    }

    @Override
    public IPage<BetOrderRecordVO> getBetOrderRecord(BetOrderRecordForm form) {
        int l = (int) (form.getMemberId() % 10);
        QueryWrapper<BetOrderRecordVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bet.member_id", form.getMemberId());
        queryWrapper.ge(StringUtils.isNotBlank(form.getStartTime()), "bet.transaction_time", form.getStartTime());
        queryWrapper.le(StringUtils.isNotBlank(form.getEndTime()), "bet.transaction_time", form.getEndTime());
        queryWrapper.eq(Objects.nonNull(form.getPlatformId()), "bet.platform_id", form.getPlatformId());
        queryWrapper.eq(Objects.nonNull(form.getCategory()), "c.id", form.getCategory());
//        queryWrapper.isNotNull("bet.settle_time");
        queryWrapper.groupBy("bet.platform_id", "bet.game_category_code");
        queryWrapper.orderByDesc("bet.transaction_time");

        Page<BetOrderRecordVO> betOrderRecord = betOrderMapper.getBetOrderRecord(new Page<>(form.getPageNum(), form.getPageSize()), l, queryWrapper);
        List<BetOrderRecordVO> records = betOrderRecord.getRecords();

        if (form.getMinBet() != null) {
            records = records.stream().filter(b ->
                    b.getTotalBet().compareTo(form.getMinBet()) >= 0
            ).collect(Collectors.toList());
        }
        if (form.getMaxBet() != null) {
            records = records.stream().filter(b ->
                    b.getTotalBet().compareTo(form.getMaxBet()) <= 0
            ).collect(Collectors.toList());
        }

        records.forEach(r -> r.setTotalWinLoss(r.getTotalWinLoss().multiply(new BigDecimal(-1))));

        betOrderRecord.setRecords(records);

        return betOrderRecord;
    }

    @Override
    public IPage<BetOrderDetailRecordVO> getBetOrderRecordDetail(BetOrderRecordDetailForm form) {
        int l = (int) (form.getMemberId() % 10);
        QueryWrapper<BetOrderDetailRecordVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bet.member_id", form.getMemberId());
        queryWrapper.ge(StringUtils.isNotBlank(form.getStartTime()), "bet.transaction_time", form.getStartTime());
        queryWrapper.le(StringUtils.isNotBlank(form.getEndTime()), "bet.transaction_time", form.getEndTime());
        queryWrapper.eq(Objects.nonNull(form.getPlatformId()), "bet.platform_id", form.getPlatformId());
        queryWrapper.eq(Objects.nonNull(form.getCategory()), "c.id", form.getCategory());
        queryWrapper.eq(Objects.nonNull(form.getBetState()), "bet.bet_state", form.getBetState());
        queryWrapper.eq(Objects.nonNull(form.getSettleNote()), "bet.settle_note", form.getSettleNote());
        queryWrapper.ge(Objects.nonNull(form.getMinValidBet()), "bet.valid_bet_amount", form.getMinValidBet());
        queryWrapper.le(Objects.nonNull(form.getMaxValidBet()), "bet.valid_bet_amount", form.getMaxValidBet());
        queryWrapper.ge(Objects.nonNull(form.getMinWinLoss()), "bet.win_loss", form.getMinWinLoss());
        queryWrapper.le(Objects.nonNull(form.getMaxWinLoss()), "bet.win_loss", form.getMaxWinLoss());
        queryWrapper.eq(Objects.nonNull(form.getTransactionNo()), "bet.transaction_no", form.getTransactionNo());
//        queryWrapper.isNotNull("bet.settle_time");
        queryWrapper.like(StringUtils.isNotBlank(form.getGameName()), "g.name_vi", form.getGameName());
        queryWrapper.like(StringUtils.isNotBlank(form.getGameNameEN()), "g.name_en", form.getGameNameEN());
        queryWrapper.orderByDesc("bet.transaction_time");
        return betOrderMapper.getBetOrderRecordDetail(new Page<>(form.getPageNum(), form.getPageSize()), l, queryWrapper);
    }

    @Override
    public List<TotalCountVO> getTotalCount(BetOrderRecordForm form) {
        int l = (int) (form.getMemberId() % 10);
        QueryWrapper<TotalCountVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bet.member_id", form.getMemberId());
        queryWrapper.ge(Objects.nonNull(form.getStartTime()), "bet.transaction_time", form.getStartTime());
        queryWrapper.le(Objects.nonNull(form.getEndTime()), "bet.transaction_time", form.getEndTime());
        queryWrapper.eq(Objects.nonNull(form.getPlatformId()), "bet.platform_id", form.getPlatformId());
        queryWrapper.eq(Objects.nonNull(form.getCategory()), "bet.game_category_code", form.getCategory());
        queryWrapper.groupBy("bet.game_category_code");

        StringBuffer sqlBuffer = new StringBuffer("where 1=1 ");
        if (form.getMinBet() != null) sqlBuffer.append(" and totalBetAmount>=" + form.getMinBet());
        if (form.getMaxBet() != null) sqlBuffer.append(" and totalBetAmount<=" + form.getMaxBet());

        return betOrderMapper.getTotalCount(l, queryWrapper, sqlBuffer.toString());
    }

    @Override
    public ReportBetOrderVO getBerOrderCount(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            startTime = sdft.format(startDate);
            endTime = sdft.format(endDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
//        startTime = startTime+"T00:00:00";
//        endTime = endTime+"T00:00:00";

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("transactionTime").gt(startTime).lte(endTime));

//        ValueCountAggregationBuilder betCountField = AggregationBuilders.count("betCount").field("transactionNo");
        SumAggregationBuilder betFiled = AggregationBuilders.sum("allBetAmount").field("betAmount");
//        SumAggregationBuilder validBetFiled = AggregationBuilders.sum("allValidBetAmount").field("validBetAmount");
//        SumAggregationBuilder companyField = AggregationBuilders.sum("companyWinLoss").field("winLoss");
//        SumAggregationBuilder settleField = AggregationBuilders.sum("settleAmount").field("settle");

        // given
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder() //
                .withQuery(boolQueryBuilder) //
                .withSearchType(SearchType.DEFAULT) //
                .withAggregations(betFiled)
//                .withAggregations(validBetFiled)
//                .withAggregations(companyField)
//                .withAggregations(settleField)
                .build();

        // when
        SearchHits<BetOrderDocument> hits = operations.search(searchQuery, BetOrderDocument.class);
        List<SearchHit<BetOrderDocument>> searchHits = hits.getSearchHits();
        List<Long> betMembers = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            BetOrderDocument content = (BetOrderDocument) searchHit.getContent();
            if (content.getMemberId() != null)
                betMembers.add(content.getMemberId());
        }

        BigDecimal betAmount = searchHits.stream().map(hit -> hit.getContent().getBetAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
//        Aggregations aggregations = (Aggregations) Objects.requireNonNull(hits.getAggregations()).aggregations();
//
//        Map<String, Aggregation> stringAggregationMap = aggregations.asMap();
//        ParsedSum betAmountTerms = (ParsedSum) stringAggregationMap.get("allBetAmount");
//        ParsedSum validBetTerms = (ParsedSum) stringAggregationMap.get("allValidBetAmount");
//        ParsedSum companyWinLossTerms = (ParsedSum) stringAggregationMap.get("companyWinLoss");
//        ParsedSum settleTerms = (ParsedSum) stringAggregationMap.get("settleAmount");

        return ReportBetOrderVO.builder()
                .betCount(searchHits.size())
                .allBetAmount(betAmount.setScale(2, RoundingMode.HALF_UP))
//                .allValidBetAmount(new BigDecimal(validBetTerms.getValue(), MathContext.DECIMAL64).setScale(2, RoundingMode.HALF_UP))
//                .companyWinLoss(new BigDecimal(companyWinLossTerms.getValue(), MathContext.DECIMAL64).negate().setScale(2, RoundingMode.HALF_UP))
                .betMembers(betMembers.stream().distinct().collect(Collectors.toList()))
//                .settleAmount(new BigDecimal(settleTerms.getValue(), MathContext.DECIMAL64).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    public ReportBetOrderVO getBerOrderCountBySettleDate(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            startTime = sdft.format(startDate);
            endTime = sdft.format(endDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
//        startTime = startTime+"T00:00:00";
//        endTime = endTime+"T00:00:00";

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("settleTime").gt(startTime).lte(endTime));

//        ValueCountAggregationBuilder betCountField = AggregationBuilders.count("betCount").field("transactionNo");
//        SumAggregationBuilder betFiled = AggregationBuilders.sum("allBetAmount").field("betAmount");
        SumAggregationBuilder validBetFiled = AggregationBuilders.sum("allValidBetAmount").field("validBetAmount");
//        SumAggregationBuilder companyField = AggregationBuilders.sum("companyWinLoss").field("winLoss");
        SumAggregationBuilder settleField = AggregationBuilders.sum("settleAmount").field("settle");

        // given
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder() //
                .withFilter(boolQueryBuilder) //
//                .withQuery(QueryBuilders.matchQuery("platformCode", "CMD"))
//                .withSearchType(SearchType.DEFAULT) //
//                .withAggregations(betFiled)
                .withAggregations(validBetFiled)
//                .withAggregations(companyField)
                .withAggregations(settleField)
                .build();

        // when
        SearchHits<BetOrderDocument> hits = operations.search(searchQuery, BetOrderDocument.class);
        List<SearchHit<BetOrderDocument>> searchHits = hits.getSearchHits();

        BigDecimal validBet = searchHits.stream().map(hit -> hit.getContent().getValidBetAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal settleAmount = searchHits.stream().map(hit -> hit.getContent().getSettle()).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReportBetOrderVO.builder()
                .betCount(searchHits.size())
                .allValidBetAmount(validBet.setScale(2, RoundingMode.HALF_UP))
                .settleAmount(settleAmount.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    public List<Map<String, Object>> findBetSumByMemberIds(List<Long> memberIds, LocalDateTime startTime, LocalDateTime endTime) {

        return this.getBaseMapper()
                .selectMaps(Wrappers.<BetOrder>query()
                        .select(" member_id memberId,date(transaction_time) createDate, sum(valid_bet_amount) betAmount,sum(win_loss) winLossAmount")
                        .lambda()
                        .between(BetOrder::getTransactionTime, startTime, endTime)
                        .in(BetOrder::getMemberId, memberIds)
                        .last(" group by date(transaction_time),member_id "));

    }

    @Override
    public IPage<MemberBetAmountRecordDailyVO> findDailyBetAmount(BetAmountRecordForm form) {
        LambdaQueryWrapper<MemberBetAmountRecordDaily> memberBetAmountRecordDailyLambdaQueryWrapper = new LambdaQueryWrapper<MemberBetAmountRecordDaily>()
                .eq(MemberBetAmountRecordDaily::getMemberId, form.getMemberId())
                .gt(StringUtils.isNotBlank(form.getStartTime()), MemberBetAmountRecordDaily::getSettleTime, form.getStartTime())
                .le(StringUtils.isNotBlank(form.getEndTime()), MemberBetAmountRecordDaily::getSettleTime, form.getEndTime())
                .eq(Objects.nonNull(form.getPlatformId()), MemberBetAmountRecordDaily::getPlatformId, form.getPlatformId())
                .isNotNull(MemberBetAmountRecordDaily::getSettleTime)
                .eq(StringUtils.isNotBlank(form.getCategory()), MemberBetAmountRecordDaily::getGameCategoryCode, StringUtils.isNotBlank(form.getCategory()) ? iGameCategoryService.getById(form.getCategory()).getCode() : "");

        return betOrderMapper.findDailyBetAmount(new Page<>(form.getPageNum(), form.getPageSize()), memberBetAmountRecordDailyLambdaQueryWrapper);
    }
}




