package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會員登入遊戲紀錄
 * @TableName ga_member_game_session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value ="ga_member_game_session")
public class MemberGameSession implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 會員ID
     */
    @TableField(value = "member_id")
    private Integer memberId;

    /**
     * 平台遊戲列表ID
     */
    @TableField(value = "platform_game_id")
    private Integer platformGameId;

    /**
     * 平台代碼
     */
    @TableField(value = "code")
    private String code;

    /**
     * 登入IP
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 創建時間
     */
    @TableField(value = "gmt_create")
    private LocalDateTime gmtCreate;

}