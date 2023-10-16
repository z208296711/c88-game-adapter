package com.c88.game.adapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 平台遊戲會員列表
 *
 * @TableName platform_game_member
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ga_platform_game_member")
public class PlatformGameMember extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 會員ID
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 平台code
     */
    @TableField(value = "code")
    private String code;

    /**
     * 平台ID
     */
    @TableField(value = "platform_id")
    private Long platformId;

    /**
     * 遊戲帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 遊戲密碼
     */
    @TableField(value = "password")
    private String password;

}