package com.c88.game.adapter.pojo.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "平台排序清單")
public class PlatformSortQuery {

    @NotNull(message = "平台ID不得為空")
    @Schema(title = "平台ID")
    private Integer platformId;


    @NotNull(message = "排序不得為空")
    @Schema(title = "排序")
    private Integer sort;

}
