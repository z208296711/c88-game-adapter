package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改平台遊戲排序置頂至底表單")
public class ModifyPlatformGameSortTopBottomForm {

    @Range(min = 0, max = 1, message = "修改排序方式參數錯誤")
    @Schema(title = "修改排序方式", description = "0置頂1置底")
    private Integer sortType;

    @NotNull(message = "平台遊戲ID不得為空")
    @Schema(title = "平台遊戲ID")
    private Integer id;

}
