package com.Market.cert.vo;

import lombok.Data;

@Data
public class StudentCertVO {
     private String studentName;
     private String school;
     private String studentId;

     private String idCardFront;
     private String idCardBack;

     private Byte status;
     private String statusDesc; // ⭐状态中文描述

    private String remark;
    }

