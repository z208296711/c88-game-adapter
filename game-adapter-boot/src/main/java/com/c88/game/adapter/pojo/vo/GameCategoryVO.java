package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "查詢遊戲類型表單")
public class GameCategoryVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "平台名稱")
    private String name;

    @Schema(title = "代碼")
    private String code;

    @Schema(title = "備註")
    private String note;

    @Schema(title = "是否直接轉入遊戲大廳", description = "0否1是")
    private Integer toGameLobby;

    @Schema(title = "排序")
    private Integer sort;

    @Schema(title = "WEB遊戲圖片")
    private String webImage;

    @Schema(title = "H5遊戲圖片")
    private String h5Image;

}
