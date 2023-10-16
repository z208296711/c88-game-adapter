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
@Schema(title = "找SABONG注單表單", description = "資料過濾")
public class FindSABONGBetFilterForm {

    @Schema(title = "欄位名稱")
    private String fieldName;

    @Schema(title = "欄位值1")
    private String fieldValue1;

    @Schema(title = "欄位值2")
    private String fieldValue2;

    /**
     * Date
     * DateTime
     * Boolean
     * String
     * UUID
     * Number
     * Integer
     */
    @Schema(title = "資料型態")
    private String type;

    /**
     * ge – greater than or equal
     * eq – equal
     * le – less than or equal
     * ne – not equal
     * like – like
     * between - between
     */
    @Schema(title = "查詢操作")
    private String operand;

}
