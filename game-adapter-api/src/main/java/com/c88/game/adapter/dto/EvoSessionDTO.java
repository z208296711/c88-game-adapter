package com.c88.game.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "EVO登入session物件")
public class EvoSessionDTO {

    @Schema(title = "session id")
    private String id;

    @Schema(title = "ip")
    private String ip;


}
