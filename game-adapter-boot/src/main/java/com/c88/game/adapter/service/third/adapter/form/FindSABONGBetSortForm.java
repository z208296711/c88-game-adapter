package com.c88.game.adapter.service.third.adapter.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "找SABONG注單表單",description = "排序")
public class FindSABONGBetSortForm {


    @Schema(title = "欲排序欄位")
    private String fieldName;

    @Schema(title = "欲排序欄位")
    private String sortType;


}
