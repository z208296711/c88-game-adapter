package com.c88.game.adapter.service;

import com.c88.game.adapter.pojo.entity.CategoryRebateRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author user
* @description 针对表【category_rebate_record】的数据库操作Service
* @createDate 2023-03-14 21:10:59
*/
public interface ICategoryRebateRecordService extends IService<CategoryRebateRecord> {

    int insertBatchXml(List<CategoryRebateRecord> list);

}
