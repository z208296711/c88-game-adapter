package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "FindTransferOrderForm")
public class FindTransferOrderForm extends BasePageQuery {


    @Schema(title = "帳號")
    @Parameter(description = "username",example = "帳號")
    private String username;

    @Schema(title = "轉帳單號")
    @Parameter(description = "轉帳單號",example = "TCXXXXXXX")
    private String serialNo;

    @Schema(title = "開始時間")
    @Parameter(description = "轉帳單號",example = "yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(title = "結束時間")
    @Parameter(description = "轉帳單號",example = "yyyy-MM-dd HH:mm:ss")
    private String endTime;


    @Schema(title = "平台代碼")
    @Parameter(description = "平台代碼",example = "KA")
    private String platformCode;

    @Schema(title = "狀態")
    @Parameter(description = "狀態",example = "1")
    private Integer state;

    @Schema(title = "類型")
    @Parameter(description = "類型",example = "1")
    private Integer type;

}
