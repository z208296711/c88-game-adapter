package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改遊戲列表樣式表單")
public class ModifyPlatformGameStyleForm {

    @NotNull(message = "遊戲類型代碼不得為空")
    @Schema(title = "遊戲類型代碼")
    private String gameCategoryCode;

    @NotNull(message = "樣式類型不得為空")
    @Schema(title = "樣式類型")
    private Integer styleType;

}
