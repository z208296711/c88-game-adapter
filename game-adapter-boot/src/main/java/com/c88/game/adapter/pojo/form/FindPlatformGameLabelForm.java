package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "取得遊戲列表表單", description = "適用於棋牌與電子")
public class FindPlatformGameLabelForm extends BasePageQuery {

    @NotNull(message = "標籤類型不得為空")
    @Parameter(description = "標籤類型 0全部 1熱門 2近期 3推薦遊戲的Banner", example = "0")
    @Schema(title = "標籤類型", description = "0全部 1熱門 2近期 3推薦遊戲的Banner", example = "0")
    private Integer labelType;

    @Parameter(description = "平台ID")
    @Schema(title = "平台ID")
    private Integer platformId;

    @Parameter(description = "遊戲類型ID 遊戲類型限定2棋牌5電子")
    @Schema(title = "遊戲類型ID", description = "遊戲類型限定2棋牌5電子")
    private Integer gameCategoryId;

    @NotNull(message = "使用者裝置介面不得為空")
    @Parameter(description = "使用者裝置介面 0=PC 1=H5")
    @Schema(title = "使用者裝置介面", description = "0=PC 1=H5")
    private Integer userDriver;

}
