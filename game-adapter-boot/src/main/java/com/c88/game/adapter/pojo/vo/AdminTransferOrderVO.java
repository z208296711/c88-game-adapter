package com.c88.game.adapter.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminTransferOrderVO  {
    /**
     * 
     */
    @Schema(title = "id")
    @Parameter(name = "id")
    private Long id;

    /**
     * 會員id
     */
    @Schema(title = "會員id")
    @Parameter(name = "轉帳前餘額")
    private Long memberId;

    /**
     * 會員帳號
     */
    @Schema(title = "會員帳號")
    @Parameter(name = "轉帳前餘額")
    private String username;

    /**
     * 所屬平台
     */
    @Schema(title = "所屬平台")
    @Parameter(name = "轉帳前餘額")
    private String platformCode;

    /**
     * 主帳號轉出:0, 轉入主帳號:1
     */
    @Schema(title = "主帳號轉出:0, 轉入主帳號:1")
    @Parameter(name = "轉帳前餘額")
    private Integer type;

    /**
     * 轉帳單號
     */
    @Schema(title = "轉帳單號")
    @Parameter(name = "轉帳前餘額")
    private String serialNo;

    /**
     * 轉帳前餘額
     */
    @Schema(title = "轉帳前餘額")
    @Parameter(name = "轉帳前餘額")
    private BigDecimal beforeBalance;

    /**
     * 轉帳金額
     */
    @Schema(title = "轉帳金額")
    @Parameter(name = "轉帳金額")
    private BigDecimal amount;

    /**
     * 轉帳後餘額
     */
    @Schema(title = "轉帳後餘額")
    @Parameter(name = "轉帳後餘額")
    private BigDecimal afterBalance;

    /**
     * 狀態-> 開單:0, 處理中:1, 成功:2, 失敗:3, 掉單:4, 掉單轉成功:5, 掉單轉失敗:6
     */
    @Schema(title = "狀態-> 開單:0, 處理中:1, 成功:2, 失敗:3, 掉單:4, 掉單轉成功:5, 掉單轉失敗:6")
    @Parameter(name = "狀態-> 開單:0, 處理中:1, 成功:2, 失敗:3, 掉單:4, 掉單轉成功:5, 掉單轉失敗:6")
    private Integer state;

    /**
     * 處理時間
     */
    @Schema(title = "處理時間")
    @Parameter(name = "處理時間")
    private LocalDateTime updateTime;

    /**
     * 處理人員
     */
    @Schema(title = "處理人員")
    @Parameter(name = "處理人員")
    private String updateBy;

    @Schema(title = "轉帳時間")
    @Parameter(name = "轉帳時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;
}