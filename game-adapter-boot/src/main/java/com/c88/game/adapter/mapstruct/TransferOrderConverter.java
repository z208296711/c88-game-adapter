package com.c88.game.adapter.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.game.adapter.pojo.entity.TransferOrder;
import com.c88.game.adapter.pojo.vo.AdminTransferOrderVO;
import com.c88.game.adapter.pojo.vo.H5MemberAccountTransferRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransferOrderConverter extends BaseConverter<TransferOrder, AdminTransferOrderVO> {

    @Mappings({
            @Mapping(target = "merchantName", source = "platformCode"),
            @Mapping(target = "tradeNo", source = "serialNo"),
            @Mapping(target = "status", expression = "java(List.of(2,5).contains(entity.getState())?1:2)")
    })
    H5MemberAccountTransferRecordVO toH5TransferVO(TransferOrder entity);

}
