package com.c88.game.adapter.mapper;

import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;

import java.util.List;
import java.util.Map;

/**
* @author user
* @description 针对表【member_bet_amount_record_daily】的数据库操作Mapper
* @createDate 2023-02-13 17:21:03
* @Entity com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily
*/
public interface MemberBetAmountRecordDailyMapper extends BaseMapper<MemberBetAmountRecordDaily> {

    List<MemberBetAmountRecordDailyVO> getUnRebateLists(Map<String,Object> param);

    int insertBatchXml(List<MemberBetAmountRecordDailyVO> list);
}




