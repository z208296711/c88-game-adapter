package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(title = "平台投注量記錄")
public class BetAmountRecordForm extends BasePageQuery {
    @Schema(title = "會員id")
    private Long memberId;

    @Schema(title = "開始時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String startTime;

    @Schema(title = "結束時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String endTime;

    @Schema(title = "遊戲平台")
    Integer platformId;

    @Schema(title = "遊戲類型")
    String category;
    @Schema(title = "表名後綴")
    Integer tableId;

    public Integer getTableId() {
        return (int) (memberId % 10);
    }
}
