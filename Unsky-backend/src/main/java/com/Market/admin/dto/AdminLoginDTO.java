package com.Market.admin.dto;

import lombok.Data;

@Data
public class AdminLoginDTO {

    // 管理员账号
    private String username;

    // 管理员密码
    private String password;
}
