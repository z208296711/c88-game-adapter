package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "UpdateTransferOrderForm")
public class UpdateTransferOrderForm  {

    @Schema(title = "id")
    private Long id;
    
    @Schema(title = "狀態  5:掉單轉成功 6:掉單轉失敗")
    private Integer state;

}
