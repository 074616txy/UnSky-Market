package com.Market.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin")
public class Admin {

    // 管理员ID
    private Long id;

    // 管理员账号
    private String username;

    // 管理员密码
    private String password;

    // 管理员角色
    private String role;

    // 创建时间
    private LocalDateTime createTime;
}
