package com.c88.game.adapter.mq.rabbitmq.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTransferOrderCheckSender {

    private final RabbitTemplate rabbitTemplate;
    private static final String ROUTE_KEY = "delayed_key";
    private static final String EXCHANGE_NAME = "delayed_exchange";

    /**
     * @param msg 消息
     * @param delay   延时时间，秒
     */
    public void send(Object msg,int delay){
        log.info("RabbitSender.send() msg = {}",msg);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTE_KEY, msg, message ->{
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);  //消息持久化
            message.getMessageProperties().setDelay(delay * 1000);   // 单位为毫秒
            return message;
        });
    }
}
