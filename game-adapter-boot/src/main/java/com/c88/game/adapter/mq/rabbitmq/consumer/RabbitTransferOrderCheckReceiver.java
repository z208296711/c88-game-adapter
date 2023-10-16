package com.c88.game.adapter.mq.rabbitmq.consumer;

import com.alibaba.fastjson.JSON;
import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.enums.TransferRecordStateEnum;
import com.c88.game.adapter.enums.TransferTypeEnum;
import com.c88.game.adapter.mq.rabbitmq.sender.RabbitTransferOrderCheckSender;
import com.c88.game.adapter.pojo.entity.TransferOrder;
import com.c88.game.adapter.service.ITransferOrderService;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import com.c88.payment.client.MemberBalanceClient;
import com.c88.payment.dto.AddBalanceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTransferOrderCheckReceiver {

    private final ITransferOrderService iTransferOrderService;

    private final GameAdapterExecutor gameAdapterExecutor;

    private final MemberBalanceClient memberBalanceClient;

    private final RabbitTransferOrderCheckSender rabbitTransferOrderCheckSender;

    @RabbitListener(queues = "delayed_transfer_order_queue")
    public void listen(TransferOrder transferRecord) {
        log.info("收到信息:{}", JSON.toJSONString(transferRecord));
        IGameAdapter gameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(transferRecord.getPlatformCode());
        Result<String> orderResult = gameAdapter.findTicketStatus(transferRecord.getUsername(), transferRecord.getSerialNo());
        if (Result.isSuccess(orderResult)) {
            switch (orderResult.getData()) {
                case "TRANSFER_SUCCESS":
                    transferRecord.setState(TransferRecordStateEnum.SUCCESS.getValue());
                    break;
                case "TRANSFER_FAIL":
                    AddBalanceDTO addBalanceDTO = AddBalanceDTO.builder()
                            .memberId(transferRecord.getMemberId())
                            .balance(transferRecord.getAmount())
                            .type(BalanceChangeTypeLinkEnum.WITHDRAW.getType())
                            .betRate(BigDecimal.ZERO)
                            .note(BalanceChangeTypeLinkEnum.WITHDRAW.getLabel() + "-轉帳失敗還款")
                            .build();
                    if (Objects.equals(transferRecord.getType(), TransferTypeEnum.INTO_PLATFORM.getValue())) {
                        //轉出三方
                        // 訂單成立用戶增加餘額及提領限額
                        addBalanceDTO.setBalance(addBalanceDTO.getBalance().negate());
                    } else {
                        //轉入三方
                        // 訂單成立用戶增加餘額及提領限額
                        addBalanceDTO.setBalance(addBalanceDTO.getBalance());
                    }
                    Result<BigDecimal> result = memberBalanceClient.addBalance(addBalanceDTO);
                    if (!Result.isSuccess(result)) {
                        log.error("轉帳失敗沒正常增加/回扣金額");
                    }
                    transferRecord.setState(TransferRecordStateEnum.FAIL.getValue());
                    break;
                default:
                    rabbitTransferOrderCheckSender.send(transferRecord, (int) TimeUnit.MINUTES.toSeconds(15));
                    break;
            }

            iTransferOrderService.updateById(transferRecord);
        }

        log.info("訂單確認結束:{}", JSON.toJSONString(transferRecord));
    }
}
