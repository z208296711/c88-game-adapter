package com.c88.game.adapter.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.pojo.form.FindTransferOrderForm;
import com.c88.game.adapter.pojo.form.UpdateTransferOrderForm;
import com.c88.game.adapter.pojo.vo.AdminTransferOrderVO;
import com.c88.game.adapter.service.ITransferOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "『後台』平台轉帳管理")
@RequiredArgsConstructor
@RequestMapping("/api/v1/platform/transfer/order")
public class TransferOrderController {

    private final ITransferOrderService iTransferOrderService;

    @Operation(summary = "平台轉帳單號-查詢")
    @GetMapping
    public PageResult<AdminTransferOrderVO> findTransferOrderPage(@ParameterObject FindTransferOrderForm form) {
        return PageResult.success(iTransferOrderService.findTransferOrderPage(form));
    }


    @Operation(summary = "平台轉帳單號-更新")
    @PutMapping
    public Result<Boolean> updateTransferOrder(@RequestBody UpdateTransferOrderForm form) {
        return Result.success(iTransferOrderService.updateTransferOrderState(form));
    }

}
