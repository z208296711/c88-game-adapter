package com.c88.game.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "遊戲平台")
public class PlatformDTO {

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "平台名稱")
    private String name;

    @Schema(title = "平台代碼")
    private String code;

    @Schema(title = "場館費(倍數)")
    private BigDecimal rate;

    @Schema(title = "允許轉入", description = "0否1可")
    private Integer canTransferIn;

    @Schema(title = "允許轉出", description = "0否1可")
    private Integer canTransferOut;

    @Schema(title = "維護", description = "0沒有維護 1維護中")
    private Integer maintainState;

    @Schema(title = "平台啟用狀態", description = "0停用 1啟用")
    private Integer enable;

}
