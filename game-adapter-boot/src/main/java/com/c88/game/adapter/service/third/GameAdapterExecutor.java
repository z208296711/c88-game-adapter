package com.c88.game.adapter.service.third;

import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
@RequiredArgsConstructor
public class GameAdapterExecutor {

    private final Map<String, IGameAdapter> gameAdapterMap;

    public IGameAdapter findByGamePlatFormByCode(String gamePlatform) {
        if (!gameAdapterMap.containsKey(gamePlatform)) {
            throw new IllegalArgumentException("Unknown Game Platform: " + gamePlatform);
        }
        return gameAdapterMap.get(gamePlatform);
    }

    public Map<String, IGameAdapter> findAllGameAdapter() {
        return gameAdapterMap;
    }
}
