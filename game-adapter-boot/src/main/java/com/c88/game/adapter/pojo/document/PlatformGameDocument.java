package com.c88.game.adapter.pojo.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * ES平台遊戲列表
 */
@Data
@Builder
@Document(indexName = "platform-game")
public class PlatformGameDocument {

    /**
     * ID
     */
    @Id
    private Integer id;

    /**
     * 遊戲名稱-越南語
     */
    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private String nameVi;

    /**
     * 遊戲名稱-英語
     */
    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private String nameEn;

    /**
     * 平台ID
     */
    @Field(type = FieldType.Integer)
    private Integer platformId;

    /**
     * 平台名稱
     */
    @Field(type = FieldType.Text)
    private String platformName;

    /**
     * 遊戲類型ID
     */
    @Field(type = FieldType.Integer)
    private Integer gameCategoryId;

    /**
     * 遊戲類型名稱
     */
    @Field(type = FieldType.Text)
    private String gameCategoryName;

    /**
     * 遊戲ID
     */
    @Field(type = FieldType.Keyword)
    private String gameId;

    /**
     * PC裝置可見 0不可見1可見
     */
    @Field(type = FieldType.Short)
    private Integer pcDeviceVisible;

    /**
     * H5裝置可見 0不可見1可見
     */
    @Field(type = FieldType.Short)
    private Integer h5DeviceVisible;

    /**
     * PC代碼1
     */
    @Field(type = FieldType.Text)
    private String pcCode1;

    /**
     * PC代碼2
     */
    @Field(type = FieldType.Text)
    private String pcCode2;

    /**
     * H5代碼1
     */
    @Field(type = FieldType.Text)
    private String h5Code1;

    /**
     * H5代碼2
     */
    @Field(type = FieldType.Text)
    private String h5Code2;

    /**
     * 啟用狀態 0停用1啟用
     */
    @Field(type = FieldType.Short)
    private Integer enable;

    /**
     * 遊戲排序
     */
    @Field(type = FieldType.Integer)
    private Integer gameSort;

    /**
     * 熱門遊戲 0否1是
     */
    @Field(type = FieldType.Short)
    private Integer hotFlag;

    /**
     * 推薦遊戲 0否1是
     */
    @Field(type = FieldType.Short)
    private Integer recommendFlag;

    /**
     * 遊戲圖片
     */
    @Field(type = FieldType.Keyword)
    private String gameImage;

    /**
     * 推薦圖片
     */
    @Field(type = FieldType.Keyword)
    private String recommendImage;

    /**
     * 標籤組
     */
    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private List<String> tags;

}