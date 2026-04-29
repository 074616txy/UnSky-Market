package com.Market.cert.service;

import com.Market.cert.vo.StudentCertVO;
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

    /**
     * 用户查询自己的认证信息
     * @param userId
     * @return
     */
    Result<StudentCertVO> getCertStatus(Long userId);

    /**
     * 管理员审核学生认证申请
     * 审核的是某一条具体的认证申请记录----学生认证表自己的主键id
     * @param id  student_cert 表主键 id
     * @param status  审核状态（1=通过，2=拒绝）
     * @param remark  审核备注
     * @return
     */
    Result<Void> auditCert(Long id, Byte status, String remark);
}
