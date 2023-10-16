package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(title = "修改遊戲平台熱門遊戲表單")
public class ModifyPlatformSortHotGameForm {

    @NotNull(message = "平台ID不得為空")
    @Schema(title = "平台ID")
    private Long platformId;

    @Schema(title = "遊戲類型ID")
    private List<Long> gameCategoryId;

}
