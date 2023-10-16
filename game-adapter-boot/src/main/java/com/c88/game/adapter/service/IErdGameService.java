package com.c88.game.adapter.service;


import com.c88.game.adapter.enums.TransferRecordStateEnum;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.form.ErdTransferForm;
import com.c88.payment.dto.PaymentMemberBalanceDTO;

import java.math.BigDecimal;

public interface IErdGameService {

    void createPlatformGameMember(Long memberId, String username, Platform platform);

    BigDecimal balance(PaymentMemberBalanceDTO memberBalanceDTO, String platformCode);

    TransferRecordStateEnum transferOut(PaymentMemberBalanceDTO memberBalanceDTO, ErdTransferForm form);

    TransferRecordStateEnum transferIntoPlatform(PaymentMemberBalanceDTO memberBalanceDTO, ErdTransferForm form);

}
