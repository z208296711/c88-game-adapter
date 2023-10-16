package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "取得遊戲類型列表")
public class H5GameCategoryVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "平台名稱")
    private String name;

    @Schema(title = "平台Code")
    private String code;

    @Schema(title = "是否直接轉入遊戲大廳", description = "0否1是")
    private Integer toGameLobby;

    @Schema(title = "排序")
    private Integer sort;

    // @Schema(title = "WEB遊戲圖片")
    // private String webImage;
    //
    // @Schema(title = "H5遊戲圖片")
    // private String h5Image;

    @Schema(title = "平台")
    private List<H5PlatformVO> platforms;

    @Schema(title = "樣式")
    private Integer styleType;
}
