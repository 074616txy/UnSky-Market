package com.Market.admin.service.impl;

import com.Market.admin.dto.AdminLoginDTO;
import com.Market.admin.dto.CertAuditDTO;
import com.Market.admin.entity.Admin;
import com.Market.admin.mapper.AdminMapper;
import com.Market.admin.service.AdminService;
import com.Market.admin.vo.AdminVO;
import com.Market.cert.mapper.StudentCertMapper;
import com.Market.common.entity.Product;
import com.Market.common.entity.StudentCert;
import com.Market.common.entity.User;
import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import com.Market.product.mapper.ProductMapper;
import com.Market.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;

    private final UserMapper userMapper;

    private final ProductMapper productMapper;

    private final StudentCertMapper studentCertMapper;

    // 创建一个密码加密工具 passwordEncoder，对密码进行加密和校验
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 管理员登录
     * @param adminLoginDTO
     * @return
     */
    @Override
    public Result<AdminVO> login(AdminLoginDTO adminLoginDTO) {
        // 1. 判断参数是否为空
        if (adminLoginDTO == null) {
            return Result.error("登录参数不能为空");
        }

        // 2. 判断账号是否为空
        if (!StringUtils.hasText(adminLoginDTO.getUsername())) {
            return Result.error("管理员账号不能为空");
        }

        // 3. 判断密码是否为空
        if (!StringUtils.hasText(adminLoginDTO.getPassword())) {
            return Result.error("管理员密码不能为空");
        }

        // 4. 根据账号查询管理员
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, adminLoginDTO.getUsername());

        Admin admin = adminMapper.selectOne(wrapper);
        if (admin == null) {
            return Result.error("管理员账号不存在");
        }
        // 5. 校验密码
        if (!passwordEncoder.matches(adminLoginDTO.getPassword(), admin.getPassword())) {
            return Result.error("管理员密码错误");
        }
        // 6. 生成管理员token
        String token = JwtUtil.generateAdminToken(admin.getId());

        // 7. 封装返回结果
        AdminVO adminVO = new AdminVO();
        adminVO.setId(admin.getId());
        adminVO.setUsername(admin.getUsername());
        adminVO.setRole(admin.getRole());
        adminVO.setToken(token);

        return Result.success(adminVO);
    }

    /**
     * 查询用户列表
     * @return
     */
    @Override
    public Result<List<User>> listUsers() {
        // 查询所有普通用户
        List<User> userList = userMapper.selectList(null);
        return Result.success(userList);
    }

    /**
     * 封禁用户
     * @param userId
     * @return
     */
    @Override
    public Result<Void> banUser(Long userId) {
        // 1. 判断用户ID是否为空
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        // 2. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 判断是否已经被封禁
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("用户已被封禁");
        }

        // 4. 修改状态为封禁
        user.setStatus((byte) 0);
        userMapper.updateById(user);

        return Result.success();
    }

    /**
     * 解封用户
     * @param userId
     * @return
     */
    @Override
    public Result<Void> unbanUser(Long userId) {
        // 1. 判断用户ID是否为空
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        // 2. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 判断是否已经是正常状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            return Result.error("用户已经是正常状态");
        }

        // 4. 修改状态为正常
        user.setStatus((byte) 1);
        userMapper.updateById(user);

        return Result.success();
    }

    /**
     * 管理员查询商品列表
     * @return
     */
    @Override
    public Result<List<Product>> listProducts() {
        // 管理员查询所有商品，不限制状态
        List<Product> productList = productMapper.selectList(null);
        return Result.success(productList);
    }

    /**
     * 下架商品
     * @param productId
     * @return
     */
    @Override
    public Result<Void> offShelfProduct(Long productId) {
        // 1. 判断商品ID是否为空
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }

        // 2. 查询商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 3. 判断商品是否已经下架
        if (product.getStatus() != null && product.getStatus() == 0) {
            return Result.error("商品已经下架");
        }

        // 4. 修改商品状态为下架
        product.setStatus((byte) 0);
        productMapper.updateById(product);

        return Result.success();
    }

    /**
     * 查询认证申请列表
     * @return
     */
    @Override
    public Result<List<StudentCert>> listStudentCerts() {
        // 查询所有认证申请
        List<StudentCert> certList = studentCertMapper.selectList(null);
        return Result.success(certList);
    }

    /**
     * 审核学生认证
     * @param certAuditDTO
     * @return
     */
    @Override
    public Result<Void> auditStudentCert(CertAuditDTO certAuditDTO) {
        // 1. 判断参数是否为空
        if (certAuditDTO == null || certAuditDTO.getCertId() == null) {
            return Result.error("认证ID不能为空");
        }

        // 2. 判断审核状态是否合法
        if (certAuditDTO.getStatus() == null
                || (certAuditDTO.getStatus() != 1 && certAuditDTO.getStatus() != 2)) {
            return Result.error("审核状态只能是1通过或2拒绝");
        }

        // 3. 查询认证申请是否存在
        StudentCert cert = studentCertMapper.selectById(certAuditDTO.getCertId());
        if (cert == null) {
            return Result.error("认证申请不存在");
        }

        // 4. 判断是否已经审核过
        if (cert.getStatus() != null && cert.getStatus() != 0) {
            return Result.error("该认证申请已审核");
        }

        // 5. 修改认证状态
        cert.setStatus(certAuditDTO.getStatus());
        studentCertMapper.updateById(cert);

        return Result.success();
    }
}
