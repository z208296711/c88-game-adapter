package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.pojo.entity.CategoryRebateRecord;
import com.c88.game.adapter.service.ICategoryRebateRecordService;
import com.c88.game.adapter.mapper.CategoryRebateRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author user
* @description 针对表【category_rebate_record】的数据库操作Service实现
* @createDate 2023-03-14 21:10:59
*/
@Service
@RequiredArgsConstructor
public class CategoryRebateRecordServiceImpl extends ServiceImpl<CategoryRebateRecordMapper, CategoryRebateRecord>
    implements ICategoryRebateRecordService {

    private final CategoryRebateRecordMapper memberRebateRecordMapper;

    @Override
    public int insertBatchXml(List<CategoryRebateRecord> list) {
        return memberRebateRecordMapper.insertBatchXml(list);
    }

}




