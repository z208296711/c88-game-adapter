package com.c88.game.adapter.service.third;

import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Configuration
public class GameAdapterConfiguration {

    @Bean
    public GameAdapterExecutor requestReceiver(List<IGameAdapter> requestHandlers) {
        return new GameAdapterExecutor(
                requestHandlers.stream()
                        .collect(toMap(IGameAdapter::getGamePlatformCode, Function.identity()))
        );
    }
}
