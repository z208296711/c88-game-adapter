package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "查詢平台遊戲會員表單")
public class FindPlatformGameMemberForm extends BasePageQuery {

    @Schema(title = "會員ID")
    private Integer memberId;

}
