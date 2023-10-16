package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErdTransferForm  {
    
	@Schema(title = "transfer", description = "0：转入, 1：转出")
    private int transfer = 0;
    
    @Schema(title = "platformCode", description = "第三方id")
    private String platformCode;
    
    @Schema(title = "amount", description = "转账金额")
    private BigDecimal amount;
    
    @Schema(title = "memberId", description = "會員id")
    private Long memberId;

}
