package com.c88.game.adapter.pojo.form;

import com.c88.game.adapter.pojo.query.GameSortQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "修改平台遊戲排序互換表單")
public class ModifyPlatformGameSortForm {

    @Schema(title = "修改多個排序")
    private List<GameSortQuery> gameAdapterSortRequests;

}
