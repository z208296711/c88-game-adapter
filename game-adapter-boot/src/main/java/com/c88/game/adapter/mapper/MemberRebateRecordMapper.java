package com.c88.game.adapter.mapper;

import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.entity.MemberRebateRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author user
* @description 针对表【member_rebate_record】的数据库操作Mapper
* @createDate 2023-03-08 17:46:00
* @Entity com.c88.game.adapter.pojo.entity.MemberRebateRecord
*/
public interface MemberRebateRecordMapper extends BaseMapper<MemberRebateRecord> {
    int insertBatchXml(List<MemberRebateRecord> list);

}




