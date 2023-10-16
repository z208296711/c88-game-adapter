package com.c88.game.adapter.consumer;

import com.alibaba.fastjson.JSON;
import com.c88.game.adapter.event.LaunchGameEvent;
import com.c88.game.adapter.event.model.LaunchGameModel;
import com.c88.game.adapter.pojo.entity.MemberGameSession;
import com.c88.game.adapter.service.IMemberGameSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LunchGameListener {

    private final IMemberGameSessionService iMemberGameSessionService;

    @Async("taskExecutor")
    @EventListener
    public void onLaunchGameEvent(LaunchGameEvent event) {
        log.info("啟動遊戲:{}", JSON.toJSONString(event.getLaunchGameModel()));

        LaunchGameModel model = event.getLaunchGameModel();

        MemberGameSession session = new MemberGameSession();
        session.setMemberId(model.getMemberId());
        session.setPlatformGameId(model.getPlatformGameId());
        session.setCode(model.getPlatformCode());
        session.setIp(model.getIp());
        session.setGmtCreate(model.getGmtCreate());
        iMemberGameSessionService.save(session);
    }
}
