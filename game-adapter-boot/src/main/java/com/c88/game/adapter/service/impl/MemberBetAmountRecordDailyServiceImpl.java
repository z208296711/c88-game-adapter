package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import com.c88.game.adapter.service.IMemberBetAmountRecordDailyService;
import com.c88.game.adapter.mapper.MemberBetAmountRecordDailyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author user
* @description 针对表【member_bet_amount_record_daily】的数据库操作Service实现
* @createDate 2023-02-13 17:21:03
*/
@Service
@RequiredArgsConstructor
public class MemberBetAmountRecordDailyServiceImpl extends ServiceImpl<MemberBetAmountRecordDailyMapper, MemberBetAmountRecordDaily>
    implements IMemberBetAmountRecordDailyService {

    private final MemberBetAmountRecordDailyMapper memberBetAmountRecordDailyMapper;

    @Override
    public List<MemberBetAmountRecordDailyVO> getUnRebateLists(Map<String,Object> param) {
        return memberBetAmountRecordDailyMapper.getUnRebateLists(param);
    }

    @Override
    public int insertBatchXml(List<MemberBetAmountRecordDailyVO> list) {
        return memberBetAmountRecordDailyMapper.insertBatchXml(list);
    }
}




