package com.c88.game.adapter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "PG登入驗證回傳成功物件")
public class PGVerifySessionDataVO {

    private String player_name;

    private String currency;

}
