package com.c88.game.adapter.service;

import com.c88.game.adapter.pojo.entity.MemberRebateRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author user
* @description 针对表【member_rebate_record】的数据库操作Service
* @createDate 2023-03-08 17:46:00
*/
public interface IMemberRebateRecordService extends IService<MemberRebateRecord> {
    int insertBatchXml(List<MemberRebateRecord> list);
}
