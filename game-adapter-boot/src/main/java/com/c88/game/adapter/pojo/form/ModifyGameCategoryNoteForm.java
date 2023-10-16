package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "修改遊戲類型備註")
public class ModifyGameCategoryNoteForm {

    @Schema(title = "遊戲類型ID")
    private Integer id;

    @Schema(title = "遊戲類型備註")
    private String note;

    @Schema(title = "遊戲類型名稱")
    private String name;

}
