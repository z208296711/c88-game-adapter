package com.c88.game.adapter.pojo.form;

import com.c88.game.adapter.pojo.query.PlatformSortQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Data
@Schema(title = "修改平台排序表單")
public class ModifyPlatformSortForm {

    @NotNull(message = "遊戲類型ID不得為空")
    @Schema(title = "遊戲類型ID")
    private Integer gameCategoryId;

    @Schema(title = "修改多個排序")
    private List<PlatformSortQuery> platformSortQueries = Collections.emptyList();

}
