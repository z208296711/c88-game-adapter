package com.c88.game.adapter.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "同步三方轉帳餘額form")
public class SyncBalanceToPlatformForm {

    @NotNull(message = "平台ID不得為空")
    @Schema(title = "平台ID")
        private Long platformId;


}
