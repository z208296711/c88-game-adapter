package com.c88.game.adapter.controller.h5;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.core.enums.AccountRecordTimeTypeEnum;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.MemberUtils;
import com.c88.game.adapter.enums.MemberAccountTransferRecordStatusEnum;
import com.c88.game.adapter.enums.TransferRecordStateEnum;
import com.c88.game.adapter.mapstruct.TransferOrderConverter;
import com.c88.game.adapter.pojo.entity.TransferOrder;
import com.c88.game.adapter.pojo.form.FindMemberAccountTransferRecordForm;
import com.c88.game.adapter.pojo.vo.H5MemberAccountTransferRecordVO;
import com.c88.game.adapter.service.ITransferOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@Tag(name = "『前台』轉帳相關")
@RequiredArgsConstructor
@RequestMapping("/h5/transfer/order")
public class H5TransferOrderController {

    private final ITransferOrderService iTransferOrderService;

    private final TransferOrderConverter transferOrderConverter;

    @Operation(summary = "帳務紀錄-轉帳", description = "轉帳")
    @GetMapping(value = "/member/account/transfer/record")
    public PageResult<H5MemberAccountTransferRecordVO> findMemberAccountTransferRecord(@Validated @ParameterObject FindMemberAccountTransferRecordForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }



        return PageResult.success(
                iTransferOrderService.lambdaQuery()
                        .eq(TransferOrder::getMemberId,memberId)
                        .in(MemberAccountTransferRecordStatusEnum.getEnum(form.getStatus()) == MemberAccountTransferRecordStatusEnum.ALL, TransferOrder::getState,
                                TransferRecordStateEnum.SUCCESS.getValue(),
                                TransferRecordStateEnum.FAIL.getValue(),
                                TransferRecordStateEnum.MISS.getValue(),
                                TransferRecordStateEnum.MISS_TO_SUCCESS.getValue(),
                                TransferRecordStateEnum.MISS_TO_FAIL.getValue()
                        )
                        .in(MemberAccountTransferRecordStatusEnum.getEnum(form.getStatus()) == MemberAccountTransferRecordStatusEnum.SUCCESS, TransferOrder::getState,
                                TransferRecordStateEnum.SUCCESS.getValue(),
                                TransferRecordStateEnum.MISS_TO_SUCCESS.getValue()
                        )
                        .in(MemberAccountTransferRecordStatusEnum.getEnum(form.getStatus()) == MemberAccountTransferRecordStatusEnum.FAIL, TransferOrder::getState,
                                TransferRecordStateEnum.FAIL.getValue(),
                                TransferRecordStateEnum.MISS.getValue(),
                                TransferRecordStateEnum.MISS_TO_FAIL.getValue()
                        )
                        .between(BaseEntity::getGmtCreate, AccountRecordTimeTypeEnum.getStartDateTime(form.getTimeType()), AccountRecordTimeTypeEnum.getEndDateTime(form.getTimeType()))
                        .orderByDesc(TransferOrder::getId)
                        .page(new Page<>(form.getPageNum(), form.getPageSize()))
                        .convert(transferOrderConverter::toH5TransferVO)
        );
    }

}
