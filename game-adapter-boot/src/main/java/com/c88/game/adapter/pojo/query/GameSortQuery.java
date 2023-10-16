package com.c88.game.adapter.pojo.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "遊戲排序清單")
public class GameSortQuery {

    @NotNull(message = "ID不得為空")
    @Schema(title = "ID")
    private Integer id;

    @NotNull(message = "排序不得為空")
    @Schema(title = "排序")
    private Integer sort;

}
