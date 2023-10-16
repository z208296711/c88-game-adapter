package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "熱門遊戲選項")
public class PlatformHotGameVO {

    @Schema(title = "熱門遊戲類型ID")
    private Integer gameCategoryId;

    @Schema(title = "熱門遊戲類型Code", description = "i18n")
    private String gameCategoryCode;

    @Schema(title = "是否為熱門遊戲", description = "0否 1是")
    private Integer isHot;

}
