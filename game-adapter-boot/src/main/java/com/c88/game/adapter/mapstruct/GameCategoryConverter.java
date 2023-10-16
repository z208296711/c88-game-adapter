package com.c88.game.adapter.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.game.adapter.pojo.entity.GameCategory;
import com.c88.game.adapter.pojo.vo.GameCategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameCategoryConverter extends BaseConverter<GameCategory, GameCategoryVO> {
}
