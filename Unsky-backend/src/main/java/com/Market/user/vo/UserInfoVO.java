package com.Market.user.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;

    private String nickname;

    private String phone;

    private String avatar;

    private String school;

    private String studentId;

    private Byte authStatus;

    private Integer creditScore;
}
