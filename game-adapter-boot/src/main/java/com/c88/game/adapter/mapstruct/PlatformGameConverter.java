package com.c88.game.adapter.mapstruct;

import cn.hutool.core.collection.CollUtil;
import com.c88.common.core.base.BaseConverter;
import com.c88.game.adapter.pojo.entity.PlatformGame;
import com.c88.game.adapter.pojo.vo.PlatformGameVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatformGameConverter extends BaseConverter<PlatformGame, PlatformGameVO> {

    // str转list
    default List<String> str2List(String src) {
        String[] split = src.split(",");
        List<String> result = Arrays.asList(split);
        return result;
    }

    // list转str
    default String list2Str(List<String> src) {
        if (CollUtil.isEmpty(src)) {
            return "";
        }
        return src.stream()
                .collect(Collectors.joining(","));
    }

}
