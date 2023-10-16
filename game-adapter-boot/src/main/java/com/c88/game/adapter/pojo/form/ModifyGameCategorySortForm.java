package com.c88.game.adapter.pojo.form;

import com.c88.game.adapter.pojo.query.GameSortQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "修改遊戲類型排序")
public class ModifyGameCategorySortForm {

    @Schema(title = "修改多個排序")
    private List<GameSortQuery> gameAdapterSortRequests;

    @Schema(title = "被移動的遊戲類型id")
    private Integer id;

    @Schema(title = "被移動的遊戲類型名稱")
    private String name;


}
