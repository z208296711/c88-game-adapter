package com.c88.game.adapter.event;

import com.c88.game.adapter.event.model.LaunchGameModel;
import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class LaunchGameEvent extends ApplicationEvent {

    private final LaunchGameModel launchGameModel;

    public LaunchGameEvent(Object source) {
        super(source);
        this.launchGameModel = (LaunchGameModel) source;
    }

}
