package com.c88.game.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "查詢所有平台遊戲VO")
public class GameVO {

    @Schema(title = "三方遊戲ID")
    private String id;
    @Schema(title = "後台遊戲名稱")
    private String name;

}
