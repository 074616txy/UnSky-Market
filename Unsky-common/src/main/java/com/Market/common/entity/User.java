package com.Market.common.entity;

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
    //主键ID，自增
    @TableId(type = IdType.AUTO)
    private Long id;
    //昵称
    private String nickname;
    //电话
    private String phone;
    //密码
    private String password;
    //头像
    private String avatar;
    //学校
    private String school;
    //学号
    private String studentId;
    // 认证状态（0=未认证 1=已认证 2=认证中）
    private Byte authStatus;
    //信誉分
    private Integer creditScore;
    //申请时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
