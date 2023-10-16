package com.c88.game.adapter.service.third.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class JiLiPageVO {

    @ApiModelProperty("當前頁數")
    @JsonAlias("CurrentPage")
    private int currentPage;
    @ApiModelProperty("總頁數")
    @JsonAlias("TotalPages")
    private int totalPages;
    @ApiModelProperty("每頁筆數")
    @JsonAlias("PageLimit")
    private int pageLimit;
    @ApiModelProperty("總筆數")
    @JsonAlias("TotalNumber")
    private long totalNumber;
}
