package com.c88.game.adapter.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "取得平台")
public class H5PlatformVO {

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "遊戲類型ID")
    private Long gameCategoryId;

    @Schema(title = "平台名稱")
    private String name;

    @Schema(title = "維護", description = "0沒有維護1維護中")
    private Integer maintainState;

    // @Schema(title = "平台圖片")
    // private String image;
    //
    // @Schema(title = "平台選擇圖片")
    // private String selectImage;

    @Schema(title = "平台代碼")
    private String platformCode;

    @Schema(title = "遊戲類型代碼")
    private String gameCategoryCode;

    @Schema(title = "平台參數-for 大廳")
    private String params;

    @Schema(title = "是否直接轉入遊戲大廳", description = "0否1是")
    private Integer toGameLobby;

    @Schema(title = "排序")
    private Integer sort;

}
