package com.c88.game.adapter.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.game.adapter.dto.PlatformDTO;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.vo.PlatformRateVO;
import com.c88.game.adapter.pojo.vo.PlatformVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatformConverter extends BaseConverter<Platform, PlatformVO> {

    @Mapping(target = "rate",expression = "java(entity.getRate().multiply(new java.math.BigDecimal(\"100\")))")
    PlatformRateVO toPlatformRateVO(Platform entity);

    PlatformDTO toDTO(Platform entity);
}
