package com.c88.game.adapter.service.impl;

import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.common.core.result.Result;
import com.c88.common.web.exception.BizException;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.TransferRecordStateEnum;
import com.c88.game.adapter.enums.TransferTypeEnum;
import com.c88.game.adapter.mq.rabbitmq.sender.RabbitTransferOrderCheckSender;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.pojo.entity.TransferOrder;
import com.c88.game.adapter.pojo.form.ErdTransferForm;
import com.c88.game.adapter.service.IErdGameService;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.ITransferOrderService;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.payment.client.MemberBalanceClient;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.TRANSFER_INTO_PLATFORM;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.TRANSFER_INTO_THIRD;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErdGameServiceImpl implements IErdGameService {

    private final RabbitTransferOrderCheckSender rabbitTransferOrderCheckSender;
    private final ITransferOrderService iTransferOrderService;
    private final GameAdapterExecutor gameAdapterExecutor;
    private final IPlatformGameMemberService iPlatformGameMemberService;
    private final MemberBalanceClient memberBalanceClient;

    @Override
    public void createPlatformGameMember(Long memberId, String username, Platform platform) {
        // 查詢遊戲帳號，無帳號時註冊帳號
        PlatformGameMember platformGameMember = iPlatformGameMemberService.lambdaQuery()
                .eq(PlatformGameMember::getMemberId, memberId)
                .eq(PlatformGameMember::getCode, platform.getCode())
                .one();

        if (platformGameMember == null) {
            IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(platform.getCode());
            Result<String> register = currentGameAdapter.register(memberId, username, platform);
            if (!Result.isSuccess(register)) {
                throw new BizException("創建遊戲帳號失敗");
            }
        }
    }

    @Override
    public BigDecimal balance(PaymentMemberBalanceDTO memberBalanceDTO, String platformCode) {
        IGameAdapter lastGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(platformCode);
        Result<BigDecimal> balanceResult = lastGameAdapter.balance(memberBalanceDTO.getUsername());
        return Result.isSuccess(balanceResult) ? balanceResult.getData() : BigDecimal.ZERO;
    }

    @Override
    public TransferRecordStateEnum transferOut(PaymentMemberBalanceDTO memberBalanceDTO, ErdTransferForm form) {
        // 先扣減主帳號金額，再發起轉帳
        Result<BigDecimal> booleanResult = memberBalanceClient.addBalance(
                AddBalanceDTO.builder()
                        .memberId(memberBalanceDTO.getMemberId())
                        .balance(form.getAmount().negate())
                        .type(TRANSFER_INTO_THIRD.getType())
                        .betRate(BigDecimal.ZERO)
                        .note(form.getPlatformCode() + "/" + TRANSFER_INTO_THIRD.getI18n())
                        .build()
        );

        if (!Result.isSuccess(booleanResult)) {
            return TransferRecordStateEnum.FAIL;
        }
        return this.callErdGameTransfer(memberBalanceDTO, form);
    }

    public TransferRecordStateEnum transferIntoPlatform(PaymentMemberBalanceDTO memberBalanceDTO, ErdTransferForm form) {

        TransferRecordStateEnum transferRecordStateEnum = this.callErdGameTransfer(memberBalanceDTO, form);
        if (transferRecordStateEnum.equals(TransferRecordStateEnum.SUCCESS)) {
            //成功回應後再增加, 主帳號金額
            memberBalanceClient.addBalance(
                    AddBalanceDTO.builder()
                            .memberId(memberBalanceDTO.getMemberId())
                            .balance(form.getAmount())
                            .type(TRANSFER_INTO_PLATFORM.getType())
                            .betRate(BigDecimal.ZERO)
                            .note(form.getPlatformCode() + "/" + TRANSFER_INTO_PLATFORM.getI18n())
                            .build()
            );
        }
        return transferRecordStateEnum;
    }

    private TransferRecordStateEnum callErdGameTransfer(PaymentMemberBalanceDTO memberBalanceDTO, ErdTransferForm form) {
        // 產生轉帳記錄
        TransferOrder transferRecord = iTransferOrderService.generateTransferOrder(memberBalanceDTO, form);

        IGameAdapter currentGameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(form.getPlatformCode());
        //三方上/下分
        Result<TransferStateVO> transferRes;
        if (form.getTransfer() == TransferTypeEnum.INTO_PLATFORM.getValue()) {
            //轉入平台
            transferRes = currentGameAdapter.transferIntoPlatform(memberBalanceDTO.getUsername(), form.getAmount(), transferRecord.getSerialNo());
        } else {
            //轉出三方
            transferRes = currentGameAdapter.transferIntoThird(memberBalanceDTO.getUsername(), form.getAmount(), transferRecord.getSerialNo());
        }
//        if (!Result.isSuccess(transferRes)) {
//            transferRecord.setState(TransferRecordStateEnum.MISS.getValue());
//            iTransferOrderService.updateById(transferRecord);
//            return TransferRecordStateEnum.FAIL;
//        }
        TransferStateVO stateVO = transferRes.getData();
        switch (stateVO.getState()) {
            case SUCCESS:
                transferRecord.setState(TransferRecordStateEnum.SUCCESS.getValue());
                break;
//            case UNKNOWN:
//                transferRecord.setState(TransferRecordStateEnum.MISS.getValue());
//                break;
            case IN_PROGRESS:
                transferRecord.setState(TransferRecordStateEnum.IN_PROGRESS.getValue());
                rabbitTransferOrderCheckSender.send(transferRecord, (int) TimeUnit.MINUTES.toSeconds(15));
                break;
            case FAIL:
                transferRecord.setState(TransferRecordStateEnum.FAIL.getValue());
                break;
            default:
                transferRecord.setState(TransferRecordStateEnum.MISS.getValue());
                break;
        }
        iTransferOrderService.updateById(transferRecord);

        //如果轉入三方失敗，則轉回金額
        if(stateVO.getState() != AdapterTransferStateEnum.SUCCESS && form.getTransfer() == TransferTypeEnum.INTO_THIRD.getValue()){
            AddBalanceDTO addBalanceDTO = AddBalanceDTO.builder()
                    .memberId(transferRecord.getMemberId())
                    .balance(transferRecord.getAmount())
                    .type(BalanceChangeTypeLinkEnum.TRANSFER_FAIL.getType())
                    .betRate(BigDecimal.ZERO)
                    .note(BalanceChangeTypeLinkEnum.TRANSFER_FAIL.getI18n())
                    .build();

            Result<BigDecimal> result = memberBalanceClient.addBalance(addBalanceDTO);
            if (!Result.isSuccess(result)) {
                log.error("轉帳失敗沒正常增加/回扣金額");
            }
        }
        return TransferRecordStateEnum.fromIntValue(transferRecord.getState());
    }
}
