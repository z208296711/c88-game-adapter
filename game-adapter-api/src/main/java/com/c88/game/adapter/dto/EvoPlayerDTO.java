package com.c88.game.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "EVO會員登入註冊物件")
public class EvoPlayerDTO {

    @Schema(title = "ID")
    private String id;

    @Schema(title = "平台名稱")
    private String firstName;

    @Schema(title = "代碼")
    private String lastName;

    @Schema(title = "國家")
    private String country;

    @Schema(title = "貨幣")
    private String currency;

    @Schema(title = "session")
    private EvoSessionDTO session = new EvoSessionDTO();

    @Schema(title = "語系")
    private String language;

    @Schema(title = "是否更新玩家資料")
    private Boolean update;
}
