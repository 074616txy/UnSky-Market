package com.Market.user.vo;

import com.Market.common.entity.User;
import lombok.Data;
import org.apache.el.parser.Token;

@Data //自动生成需要使用的方法
public class LoginVO {

    private User user;
    private String token;
}