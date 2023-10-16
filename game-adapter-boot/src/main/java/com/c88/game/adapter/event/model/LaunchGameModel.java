package com.c88.game.adapter.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaunchGameModel {

    private Integer memberId;
    private String ip;
    private String username;
    private Integer platformId;
    private Integer platformGameId;
    private String platformCode;

    private LocalDateTime gmtCreate;

}
