package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.event.BetRecord;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.form.BetAmountRecordForm;
import com.c88.game.adapter.pojo.form.BetOrderRecordDetailForm;
import com.c88.game.adapter.pojo.form.BetOrderRecordForm;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import com.c88.game.adapter.pojo.vo.TotalCountVO;
import com.c88.game.adapter.pojo.vo.BetOrderDetailRecordVO;
import com.c88.game.adapter.pojo.vo.BetOrderRecordVO;
import com.c88.game.adapter.vo.ReportBetOrderVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* @author user
* @description 针对表【ga_bet_order(會員注單紀錄)】的数据库操作Service
* @createDate 2022-05-18 11:34:13
*/
public interface IBetOrderService extends IService<BetOrder> {

    BetRecord insertOrUpdate(BetOrder betOrder);

    BigDecimal findMemberNonSettleBetAmount(Long id);

    IPage<BetOrderRecordVO> getBetOrderRecord(BetOrderRecordForm form);

    IPage<BetOrderDetailRecordVO> getBetOrderRecordDetail(BetOrderRecordDetailForm form);

    List<TotalCountVO> getTotalCount(BetOrderRecordForm form);

    ReportBetOrderVO getBerOrderCount(String startTime, String endTime);

    ReportBetOrderVO getBerOrderCountBySettleDate(String startTime, String endTime);

    List<Map<String,Object>> findBetSumByMemberIds(List<Long> memberIds, LocalDateTime start, LocalDateTime end);

    IPage<MemberBetAmountRecordDailyVO> findDailyBetAmount(BetAmountRecordForm form);

}
