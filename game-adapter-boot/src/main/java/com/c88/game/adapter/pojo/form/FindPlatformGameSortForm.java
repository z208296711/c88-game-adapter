package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "查詢平台遊戲排序內容表單")
public class FindPlatformGameSortForm extends BasePageQuery {

    @NotNull(message = "平台ID不得為空")
    @Schema(title = "平台ID")
    private Integer platformId;

    @NotNull(message = "遊戲類型ID不得為空")
    @Schema(title = "遊戲類型ID")
    private Integer gameCategoryId;

    @Schema(title = "遊戲名稱")
    private String name;

}
