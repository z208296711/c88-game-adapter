package com.c88.game.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "PG登入驗證物件")
public class PGVerifySessionDTO {

    private String operator_token;

    private String secret_key;

    private String operator_player_session;


}
