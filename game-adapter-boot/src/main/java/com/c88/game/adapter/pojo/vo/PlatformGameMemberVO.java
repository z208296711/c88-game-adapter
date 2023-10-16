package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "查詢平台遊戲會員表單")
public class PlatformGameMemberVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "平台code")
    private String code;

    @Schema(title = "平台ID")
    private Long platformId;

    @Schema(title = "遊戲帳號")
    private String username;

    @Schema(title = "遊戲密碼")
    private String password;

}
