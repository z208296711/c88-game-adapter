package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.TransferOrder;
import com.c88.game.adapter.pojo.form.ErdTransferForm;
import com.c88.game.adapter.pojo.form.FindTransferOrderForm;
import com.c88.game.adapter.pojo.form.UpdateTransferOrderForm;
import com.c88.game.adapter.pojo.vo.AdminTransferOrderVO;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;

/**
 *
 */
public interface ITransferOrderService extends IService<TransferOrder> {

    IPage<AdminTransferOrderVO> findTransferOrderPage(FindTransferOrderForm form);

    TransferOrder generateTransferOrder(PaymentMemberBalanceDTO memberBalanceDTO, ErdTransferForm form);

    Boolean updateTransferOrderState(UpdateTransferOrderForm form);
}
