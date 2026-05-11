package com.Market.admin.controller;

import com.Market.admin.dto.AdminLoginDTO;
import com.Market.admin.dto.CertAuditDTO;
import com.Market.admin.service.AdminService;
import com.Market.admin.vo.AdminVO;
import com.Market.common.entity.Product;
import com.Market.common.entity.StudentCert;
import com.Market.common.entity.User;
import com.Market.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 管理员登录
     * @param adminLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<AdminVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {
        return adminService.login(adminLoginDTO);
    }

    /**
     * 查询用户列表
     * @return
     */
    @GetMapping("/users")
    public Result<List<User>> listUsers() {
        return adminService.listUsers();
    }

    /**
     * 封禁用户
     * @param userId
     * @return
     */
    @PutMapping("/users/ban/{userId}")
    public Result<Void> banUser(@PathVariable Long userId) {
        return adminService.banUser(userId);
    }

    /**
     * 解封用户
     * @param userId
     * @return
     */
    @PutMapping("/users/unban/{userId}")
    public Result<Void> unbanUser(@PathVariable Long userId) {
        return adminService.unbanUser(userId);
    }

    /**
     * 管理员查询商品列表
     * @return
     */
    @GetMapping("/products")
    public Result<List<Product>> listProducts() {
        return adminService.listProducts();
    }

    /**
     * 下架商品
     * @param productId
     * @return
     */
    @PutMapping("/products/off/{productId}")
    public Result<Void> offShelfProduct(@PathVariable Long productId) {
        return adminService.offShelfProduct(productId);
    }

    /**
     * 查询学生认证申请列表
     * @return
     */
    @GetMapping("/certifications")
    public Result<List<StudentCert>> listStudentCerts() {
        return adminService.listStudentCerts();
    }

    /**
     * 审核学生认证
     * @param certAuditDTO
     * @return
     */
    @PutMapping("/certifications/audit")
    public Result<Void> auditStudentCert(@RequestBody CertAuditDTO certAuditDTO) {
        return adminService.auditStudentCert(certAuditDTO);
    }
}
