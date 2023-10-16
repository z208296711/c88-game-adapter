package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class FindPlatformGameForm extends BasePageQuery {

    @Parameter(description = "遊戲平台ID")
    private Integer platformId;

    @Parameter(description = "遊戲類型ID")
    private Integer gameCategoryId;

    @Parameter(description = "遊戲啟用狀態 0停用1啟用")
    private Integer enable;

    @Parameter(description = "遊戲類型 0熱門遊戲1推薦遊戲")
    private Integer gameType;

    @Parameter(description = "關鍵字搜尋各欄位")
    private String keyword;

}
