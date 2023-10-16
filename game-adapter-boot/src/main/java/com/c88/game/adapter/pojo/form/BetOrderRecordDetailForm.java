package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "注單記錄")
public class BetOrderRecordDetailForm extends BasePageQuery {
    @Schema(title = "會員id")
    private Long memberId;

    @Schema(title = "開始時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String startTime;

    @Schema(title = "結束時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String endTime;

    @Schema(title = "遊戲平台")
    Integer platformId;

    @Schema(title = "遊戲類型")
    Integer category;

    @Schema(title = "金額下限")
    BigDecimal minWinLoss;

    @Schema(title = "金額上限")
    BigDecimal maxWinLoss;

    @Schema(title = "狀態 0:未派彩, 1:已派彩")
    Integer betState;

    @Schema(title = "已結算備註 1已取消 2退款投注 3兌現")
    Integer settleNote;

    @Schema(title = "金額下限")
    BigDecimal minValidBet;

    @Schema(title = "金額上限")
    BigDecimal maxValidBet;

    @Schema(title = "註單編號")
    String transactionNo;

    @Schema(title = "遊戲名稱")
    String gameName;

    @Schema(title = "英文遊戲名稱")
    String gameNameEN;
}
