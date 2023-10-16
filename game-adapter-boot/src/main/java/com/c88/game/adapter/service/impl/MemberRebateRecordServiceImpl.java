package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.entity.MemberRebateRecord;
import com.c88.game.adapter.service.IMemberRebateRecordService;
import com.c88.game.adapter.mapper.MemberRebateRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author user
* @description 针对表【member_rebate_record】的数据库操作Service实现
* @createDate 2023-03-08 17:46:00
*/
@Service
@RequiredArgsConstructor
public class MemberRebateRecordServiceImpl extends ServiceImpl<MemberRebateRecordMapper, MemberRebateRecord>
    implements IMemberRebateRecordService {
    private final MemberRebateRecordMapper memberRebateRecordMapper;

    @Override
    public int insertBatchXml(List<MemberRebateRecord> list) {
        return memberRebateRecordMapper.insertBatchXml(list);
    }

}




