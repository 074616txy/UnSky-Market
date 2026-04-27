package com.Market.cert.service;

import com.Market.common.entity.StudentCert;
import com.Market.common.result.Result;

public interface StudentCertService {

    // 根据用户ID查询认证信息
    StudentCert getByUserId(Long userId);

    /**
     * 用户提交学生认证信息
     * @param userId
     * @param studentCert
     * @return
     */
    Result<Void> submitCert(Long userId, StudentCert studentCert);
}
