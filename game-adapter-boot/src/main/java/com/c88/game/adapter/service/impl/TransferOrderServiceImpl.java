package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.UUIDUtils;
import com.c88.common.web.util.UserUtils;
import com.c88.game.adapter.enums.TransferRecordStateEnum;
import com.c88.game.adapter.enums.TransferRecordTypeEnum;
import com.c88.game.adapter.enums.TransferTypeEnum;
import com.c88.game.adapter.mapper.TransferRecordMapper;
import com.c88.game.adapter.mapstruct.TransferOrderConverter;
import com.c88.game.adapter.pojo.entity.TransferOrder;
import com.c88.game.adapter.pojo.form.ErdTransferForm;
import com.c88.game.adapter.pojo.form.FindTransferOrderForm;
import com.c88.game.adapter.pojo.form.UpdateTransferOrderForm;
import com.c88.game.adapter.pojo.vo.AdminTransferOrderVO;
import com.c88.game.adapter.service.ITransferOrderService;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;

/**
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferOrderServiceImpl extends ServiceImpl<TransferRecordMapper, TransferOrder> implements ITransferOrderService {

    private final TransferOrderConverter transferOrderConverter;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public IPage<AdminTransferOrderVO> findTransferOrderPage(FindTransferOrderForm form) {
        return this.lambdaQuery()
                .ge(StringUtils.isNotBlank(form.getStartTime()), TransferOrder::getGmtCreate, form.getStartTime())
                .le(StringUtils.isNotBlank(form.getEndTime()), TransferOrder::getGmtCreate, form.getEndTime())
                .eq(StringUtils.isNotBlank(form.getUsername()), TransferOrder::getUsername, form.getUsername())
                .eq(StringUtils.isNotBlank(form.getSerialNo()), TransferOrder::getSerialNo, form.getSerialNo())
                .eq(StringUtils.isNotBlank(form.getPlatformCode()), TransferOrder::getPlatformCode, form.getPlatformCode())
                .eq(form.getState() != null, TransferOrder::getState, form.getState())
                .eq(form.getType() != null, TransferOrder::getType, form.getType())
                .orderByDesc(TransferOrder::getGmtCreate)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(transferOrderConverter::toVo);
    }

    @Override
    public TransferOrder generateTransferOrder(PaymentMemberBalanceDTO memberBalanceDTO, ErdTransferForm form) {
        BigDecimal last;
        //0：转入, 1：转出
        if (form.getTransfer() == 0) {
            last = memberBalanceDTO.getBalance().add(form.getAmount());
        } else {
            last = memberBalanceDTO.getBalance().subtract(form.getAmount());
        }
        TransferOrder transferRecord = new TransferOrder();
        transferRecord.setMemberId(memberBalanceDTO.getMemberId());
        transferRecord.setUsername(memberBalanceDTO.getUsername());
        transferRecord.setPlatformCode(form.getPlatformCode());
        transferRecord.setType(form.getTransfer() == 1 ? TransferRecordTypeEnum.ACCOUNT_TURN_OUT.getValue() : TransferRecordTypeEnum.ACCOUNT_TURN_IN.getValue());
        transferRecord.setSerialNo(UUIDUtils.genOrderId("TC"));
        transferRecord.setBeforeBalance(memberBalanceDTO.getBalance());
        transferRecord.setAmount(form.getAmount());
        transferRecord.setAfterBalance(last);
        transferRecord.setState(TransferRecordStateEnum.OPEN.getValue());
        this.save(transferRecord);
        return transferRecord;
    }

    @Override
    @Transactional
    public Boolean updateTransferOrderState(UpdateTransferOrderForm form) {
        TransferOrder transferRecord = Optional.ofNullable(this.getById(form.getId()))
                .filter(x -> x.getState().equals(TransferRecordStateEnum.MISS.getValue()))
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        TransferRecordStateEnum targetState = TransferRecordStateEnum.fromIntValue(form.getState());
        switch (targetState) {
            case MISS_TO_SUCCESS:
                transferRecord.setState(TransferRecordStateEnum.MISS_TO_SUCCESS.getValue());
                break;
            case MISS_TO_FAIL:
                transferRecord.setState(TransferRecordStateEnum.MISS_TO_FAIL.getValue());
                if (Objects.equals(transferRecord.getType(), TransferTypeEnum.INTO_THIRD.getValue())) {
                    // 轉出三方失敗加回
                    kafkaTemplate.send(BALANCE_CHANGE,
                            AddBalanceDTO.builder()
                            .memberId(transferRecord.getMemberId())
                            .balance(transferRecord.getAmount())
                            .type(BalanceChangeTypeLinkEnum.WITHDRAW_FAIL.getType())
                            .betRate(BigDecimal.ZERO)
                            .note(BalanceChangeTypeLinkEnum.WITHDRAW_FAIL.getI18n())
                            .build());
                }
                break;
            default:
                throw new BizException(ResultCode.PARAM_ERROR);
        }
        transferRecord.setUpdateBy(UserUtils.getUsername());
        transferRecord.setUpdateTime(LocalDateTime.now());
        transferRecord.setState(form.getState());
        return this.updateById(transferRecord);
    }
}




