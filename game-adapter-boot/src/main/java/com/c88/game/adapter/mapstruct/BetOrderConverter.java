package com.c88.game.adapter.mapstruct;

import com.c88.game.adapter.event.BetRecord;
import com.c88.game.adapter.pojo.document.BetOrderDocument;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.vo.BetOrderVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BetOrderConverter {

    BetOrderVO toVO(BetOrder entity);

    BetOrder toEntity(BetOrderVO vo);

    BetRecord toRecord(BetOrder entity);

    BetOrderDocument toDocument(BetOrder betOrder);
}
