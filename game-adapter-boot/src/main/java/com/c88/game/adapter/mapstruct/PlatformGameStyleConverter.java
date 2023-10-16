package com.c88.game.adapter.mapstruct;

import com.c88.game.adapter.pojo.entity.PlatformGameStyle;
import com.c88.game.adapter.pojo.vo.PlatformGameStyleVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatformGameStyleConverter {

    PlatformGameStyleVO toVO(PlatformGameStyle entity);

}
