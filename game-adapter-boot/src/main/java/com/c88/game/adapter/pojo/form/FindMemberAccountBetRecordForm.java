package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "查詢會員存款紀錄表單")
public class FindMemberAccountBetRecordForm extends BasePageQuery {

    @Parameter(description = "遊戲類型ID 不輸入=全部")
    @Schema(title = "遊戲類型代碼", description = "不輸入=全部")
    private String gameCategoryCode;

    @Parameter(description = "遊戲平台ID 不輸入=全部")
    @Schema(title = "遊戲平台代碼", description = "不輸入=全部")
    private String platformCode;

    @NotNull(message = "時間類型不得為空")
    @Parameter(description = "時間類型 1今日 2昨天 3七天內 4三十天內")
    @Schema(title = "時間類型", description = "1今日 2昨天 3七天內 4三十天內")
    private Integer timeType;

}
