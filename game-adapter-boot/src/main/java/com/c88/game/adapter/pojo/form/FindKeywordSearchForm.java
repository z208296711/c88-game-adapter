package com.c88.game.adapter.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class FindKeywordSearchForm extends BasePageQuery {

    @Parameter(description = "關鍵字", required = true)
    private String keyword;

    @Parameter(description = "使用者裝置介面 0=PC 1=H5", required = true)
    private Integer userDriver;

}
