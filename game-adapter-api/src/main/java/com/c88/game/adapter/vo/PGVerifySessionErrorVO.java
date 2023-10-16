package com.c88.game.adapter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "PG登入驗證回傳失敗物件")
public class PGVerifySessionErrorVO {

    private String code;

    private String message;

}
