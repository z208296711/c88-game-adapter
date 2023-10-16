package com.c88.game.adapter.service.third.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class FCGameVO {
    private String recordID;
    private String account;
    private Integer gameID;
    private Integer gametype;
    private Double bet;
    private Double winlose;
    private Double prize;
    private Integer jpmode;
    private Double jppoints;
    private Double jptax;
    private Double before;
    private Double after;
    private LocalDateTime bdate;
    private Boolean isBuyFeature;
}
