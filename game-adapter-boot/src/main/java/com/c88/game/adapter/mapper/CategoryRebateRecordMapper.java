package com.c88.game.adapter.mapper;

import com.c88.game.adapter.pojo.entity.CategoryRebateRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author user
* @description 针对表【category_rebate_record】的数据库操作Mapper
* @createDate 2023-03-14 21:10:59
* @Entity com.c88.game.adapter.pojo.entity.CategoryRebateRecord
*/
public interface CategoryRebateRecordMapper extends BaseMapper<CategoryRebateRecord> {

    int insertBatchXml(List<CategoryRebateRecord> list);
}




