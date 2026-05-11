package com.Market.admin.vo;

import lombok.Data;

@Data
public class AdminVO {

    // 管理员ID
    private Long id;

    // 管理员账号
    private String username;

    // 管理员角色
    private String role;

    // 登录 token
    private String token;
}
