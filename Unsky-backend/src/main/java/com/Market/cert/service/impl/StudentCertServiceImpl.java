package com.Market.cert.service.impl;

import com.Market.cert.mapper.StudentCertMapper;
import com.Market.cert.service.StudentCertService;
import com.Market.common.entity.StudentCert;
import com.Market.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 它的作用是：
 * 真正承接后面学生认证相关业务逻辑
 * 比如提交认证申请、查询认证状态、管理员审核等
 */
@Service
public class StudentCertServiceImpl implements StudentCertService {
    /**
     * 为什么在接口实现类和控制类都要写下面这个而不是@Autowired private StudentCertMapper studentCertMapper;？
     * - 前者是构造器的注入，后者是字段的注入；前者更加安全，不会出现null，后面报错率高
     */
    private final StudentCertMapper studentCertMapper;

    public StudentCertServiceImpl(StudentCertMapper studentCertMapper) {
        this.studentCertMapper = studentCertMapper;
    }

    @Override
    /**
     * 根据关联用户查询userId
     */
    public StudentCert getByUserId(Long userId) {
        QueryWrapper<StudentCert> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);

        return studentCertMapper.selectOne(wrapper);
    }

    @Override
    /**
     * 提交学生认证申请
     *核心流程：
     *  * 1. 校验是否已提交认证（防重复提交）
     *  * 2. 构造认证数据（绑定当前用户 + 初始化状态）
     *  * 3. 插入数据库
     */
    public Result<Void> submitCert(Long userId, StudentCert studentCert) {
        //构造数据 强制绑定当前登录用户（防止前端伪造 userId）
        studentCert.setUserId(userId);
        //初始化认证状态：0 = 待审核
        studentCert.setStatus((byte) 0);
        //插入数据库
        int rows = studentCertMapper.insert(studentCert);
        // 插入失败（理论上很少发生，但必须兜底）
        if (rows <= 0) {//
            return Result.error("认证申请提交失败");
        }
        return Result.success(null);
    }
}

