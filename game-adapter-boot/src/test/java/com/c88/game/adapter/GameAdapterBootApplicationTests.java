package com.c88.game.adapter;
//
//import com.c88.amqp.EventType;
//import com.c88.amqp.producer.MessageProducer;

import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.KAGameAdapter;
import com.c88.kafka.producer.KafkaMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active:local",webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameAdapterBootApplicationTests {

    @Autowired
    private GameAdapterExecutor gameAdapterExecutor;

    @Autowired
    private KafkaMessageProducer kafkaMessageProducer;

    @Test
    void contextLoads() {
        KAGameAdapter gameAdapter = (KAGameAdapter)gameAdapterExecutor.findByGamePlatFormByCode("KA");
        gameAdapter.doFetchBetOrderAction();
       // Assert.isNull(gameAdapter);
    }

    @Test
    void contextLoads2() {
        kafkaMessageProducer.sendMessage("twitch_chat", "test");
        System.out.println("");
    }

}
