package com.Market.cert.service.impl;

import com.Market.cert.mapper.StudentCertMapper;
import com.Market.cert.service.StudentCertService;
import com.Market.cert.vo.StudentCertVO;
import com.Market.common.entity.StudentCert;
import com.Market.common.entity.User;
import com.Market.common.result.Result;
import com.Market.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    @Autowired
    private UserMapper userMapper;

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
     * 核心流程：
     *  * 1. 校验是否已提交认证（防重复提交）后续优化
     *  * 2. 构造认证数据（绑定当前用户 + 初始化状态）
     *  * 3. 插入数据库
     */
    @Transactional // ⭐事务：保证“校验 + 插入”原子性（要么全成功，要么全失败）
    public Result<Void> submitCert(Long userId, StudentCert studentCert) {

        // ⭐防重复提交：同一用户只能有一条“待审核”记录
        LambdaQueryWrapper<StudentCert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCert::getUserId, userId)
                .eq(StudentCert::getStatus, 0); // 0 = 待审核

        Long count = studentCertMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("请勿重复提交认证申请");
        }

        // ⭐绑定当前用户（禁止前端传 userId，防伪造）
        studentCert.setUserId(userId);

        // ⭐初始化状态：0 = 待审核（状态流转起点）
        studentCert.setStatus((byte) 0);

        // 插入数据库
        int rows = studentCertMapper.insert(studentCert);

        // ⭐兜底：插入失败（极少，但必须处理）
        if (rows <= 0) {
            return Result.error("认证申请提交失败");
        }

        return Result.success(null);
    }


    /**
     * 根据当前用户id查询学生认证状态
     */
    @Override
    public Result<StudentCertVO> getCertStatus(Long userId) {
        LambdaQueryWrapper<StudentCert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCert::getUserId, userId);

        StudentCert studentCert = studentCertMapper.selectOne(wrapper);
        if (studentCert == null) {
            return Result.error("当前用户暂无认证申请记录");
        }

        // ⭐转换 VO
        StudentCertVO studentCertVO = new StudentCertVO();
        //⭐1. 自动拷贝基础字段
        BeanUtils.copyProperties(studentCert, studentCertVO);

        // ⭐2. 手动补充业务字段
        Byte status = studentCert.getStatus();
        if (status == 0) {
            studentCertVO.setStatusDesc("待审核");
        } else if (status == 1) {
            studentCertVO.setStatusDesc("已通过");
        } else if (status == 2) {
            studentCertVO.setStatusDesc("已拒绝");
        }

         // ⭐统一返回（必须最后）
        return Result.success(studentCertVO);
    }

    /**
     * 管理员审核学生认证信息
     */
    @Override
    public Result<Void> auditCert(Long id, Byte status, String remark) {

        StudentCert studentCert = studentCertMapper.selectById(id);
        if (studentCert == null) {
            return Result.error("认证申请记录不存在");
        }
            // 1. 更新认证申请状态和备注
            studentCert.setStatus(status);
            studentCert.setRemark(remark);

            int certRows = studentCertMapper.updateById(studentCert);
            if (certRows <= 0) {
                return Result.error("认证审核失败");
            }

            // 2. 同步更新用户认证状态
            User user = userMapper.selectById(studentCert.getUserId());
            if (user == null) {
                return Result.error("关联用户不存在");
            }

            user.setAuthStatus(status);
            int userRows = userMapper.updateById(user);
            if (userRows <= 0) {
                return Result.error("用户认证状态更新失败");
            }
        return Result.success(null);
    }
}

