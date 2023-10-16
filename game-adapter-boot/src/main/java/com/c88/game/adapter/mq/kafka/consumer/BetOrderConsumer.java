package com.c88.game.adapter.mq.kafka.consumer;

import com.c88.common.core.constant.TopicConstants;
import com.c88.game.adapter.enums.BetOrderEventTypeEnum;
import com.c88.game.adapter.event.BetRecord;
import com.c88.game.adapter.mapstruct.BetOrderConverter;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetOrderConsumer {

    private final IBetOrderService iBetOrderService;

    private final BetOrderConverter betOrderConverter;

    private final KafkaMessageProducer<BetRecord> kafkaMessageProducer;

    @KafkaListener(topics = "save-bet-order", groupId = "saveBetOrder")
    public void consumerSaveBet(BetOrderVO betOrderVO, Acknowledgment acknowledgement) {
        log.info("save bet message listen start, message={}", betOrderVO);
        BetOrder betOrder = betOrderConverter.toEntity(betOrderVO);

        BetRecord betRecord = iBetOrderService.insertOrUpdate(betOrder);
        kafkaMessageProducer.sendMessage(TopicConstants.SAVE_BET_ORDER_EVENT, betRecord);
        if (Objects.equals(betRecord.getEventType(), BetOrderEventTypeEnum.BET_SETTLED.getValue()) ||
                Objects.equals(betRecord.getEventType(), BetOrderEventTypeEnum.BET_UPDATE_SETTLE.getValue()) ||
                Objects.equals(betRecord.getEventType(), BetOrderEventTypeEnum.BET_CANCELED.getValue())) {// 只有第一次結算、注單狀態有變或取消的注單才會送到風控計算
            log.info("consumerSaveBet :{}, berOrderType:{}", betRecord.getTransactionNo(), betRecord.getEventType());
            kafkaMessageProducer.sendMessage(TopicConstants.BET_RECORD, betRecord.getUsername(), betRecord);
            kafkaMessageProducer.sendMessage(TopicConstants.VALID_BET, betRecord);
        }
        acknowledgement.acknowledge();
    }

}
