package com.c88.game.adapter.service.third.vo;

import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferStateVO {

    //轉帳金額
    private BigDecimal balance;

    //未確認:0, 成功: 1,  失敗:-1
    private AdapterTransferStateEnum state;

}
