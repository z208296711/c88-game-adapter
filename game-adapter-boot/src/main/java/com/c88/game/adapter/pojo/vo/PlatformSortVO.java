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
@Schema(title = "查詢平台遊戲排序內容")
public class PlatformSortVO {

    @Schema(title = "平台排序ID")
    private Long id;

    @Schema(title = "平台ID")
    private Long platformId;

    @Schema(title = "平台名稱")
    private String platformName;

    @Schema(title = "遊戲類型ID")
    private Integer gameCategoryId;

    @Schema(title = "排序")
    private Integer platformSort;

    @Schema(title = "熱門遊戲", description = "0否 1是")
    private Integer hot;

}
