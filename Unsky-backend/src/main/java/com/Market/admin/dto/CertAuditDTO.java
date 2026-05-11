package com.Market.admin.dto;

import lombok.Data;

@Data
public class CertAuditDTO {

    // 认证ID
    private Long certId;

    // 审核状态：1通过，2拒绝
    private Byte status;
}
