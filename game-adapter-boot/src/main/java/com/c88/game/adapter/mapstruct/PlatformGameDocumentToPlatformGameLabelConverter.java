package com.c88.game.adapter.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.game.adapter.pojo.document.PlatformGameDocument;
import com.c88.game.adapter.pojo.vo.PlatformGameLabelVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatformGameDocumentToPlatformGameLabelConverter extends BaseConverter<PlatformGameDocument, PlatformGameLabelVO> {
}
