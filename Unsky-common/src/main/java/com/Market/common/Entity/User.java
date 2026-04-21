package com.Market.common.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类：对应数据库 sys_user 表
 * 放置于 Common 模块，供 backend 和未来其他模块共用
 */
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nickname;

    private String phone;

    private String password;

    private String avatar;

    private String school;

    private String studentId;

    // 认证状态（0=未认证 1=已认证 2=认证中）
    private Byte authStatus;

    private Integer creditScore;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
