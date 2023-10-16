package com.c88.game.adapter.service.third.adapter.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "找SABONG注單表單")
public class FindSABONGBetForm {

    @Schema(title = "頁數")
    private Integer pageNumber;

    @Schema(title = "每頁筆數")
    private Integer pageSize;

    @Schema(title = "過濾條件")
    private List<FindSABONGBetFilterForm> filter;

    @Schema(title = "排序")
    private List<FindSABONGBetSortForm> sort;

}
