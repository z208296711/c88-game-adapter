package com.c88.game.adapter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "PG登入驗證回傳物件")
public class PGVerifySessionVO {

    private PGVerifySessionDataVO data;

    private PGVerifySessionErrorVO error;

}
