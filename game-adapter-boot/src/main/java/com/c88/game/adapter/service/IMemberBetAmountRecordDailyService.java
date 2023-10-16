package com.c88.game.adapter.service;

import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author user
* @description 针对表【member_bet_amount_record_daily】的数据库操作Service
* @createDate 2023-02-13 17:21:03
*/
public interface IMemberBetAmountRecordDailyService extends IService<MemberBetAmountRecordDaily> {

    List<MemberBetAmountRecordDailyVO> getUnRebateLists(Map<String,Object> param);

    int insertBatchXml(@Param("list")List<MemberBetAmountRecordDailyVO> list);

}
