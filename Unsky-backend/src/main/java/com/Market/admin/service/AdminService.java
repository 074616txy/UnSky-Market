package com.Market.admin.service;

import com.Market.admin.dto.AdminLoginDTO;
import com.Market.admin.dto.CertAuditDTO;
import com.Market.admin.vo.AdminVO;
import com.Market.common.entity.Product;
import com.Market.common.entity.StudentCert;
import com.Market.common.entity.User;
import com.Market.common.result.Result;

import java.util.List;

public interface AdminService {
    /**
     * 管理员登录
     * @param adminLoginDTO
     * @return
     */
    Result<AdminVO> login(AdminLoginDTO adminLoginDTO);

    /**
     * 查询用户列表
     * @return
     */
    Result<List<User>> listUsers();

    /**
     * 封禁用户
     * @param userId
     * @return
     */
    Result<Void> banUser(Long userId);

    /**
     * 解封用户
     * @param userId
     * @return
     */
    Result<Void> unbanUser(Long userId);

    /**
     * 管理员查询商品列表
     * @return
     */
    Result<List<Product>> listProducts();

    /**
     * 下架商品
     * @param productId
     * @return
     */
    Result<Void> offShelfProduct(Long productId);

    /**
     * 查询认证申请列表
     * @return
     */
    Result<List<StudentCert>> listStudentCerts();

    /**
     * 审核学生认证
     * @param certAuditDTO
     * @return
     */
    Result<Void> auditStudentCert(CertAuditDTO certAuditDTO);


}

