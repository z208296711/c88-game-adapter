package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(title = "會員帳務紀錄")
public class H5MemberAccountTransferRecordVO {

    @Schema(title = "單號")
    private String tradeNo;

    @Schema(title = "狀態")
    private Integer status;

    @Schema(title = "名稱")
    private String merchantName;

    @Schema(title = "轉出類型",description = "主帳號轉出:0, 轉入主帳號:1")
    private Integer type;

    @Schema(title = "金額")
    private BigDecimal amount;

    @Schema(title = "申請時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;

}
