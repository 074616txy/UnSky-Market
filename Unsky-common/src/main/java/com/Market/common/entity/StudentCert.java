package com.Market.common.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类：对应数据库 student_cert 表
 * 放置于 Common 模块，供 backend 和未来其他模块共用
 */
@Data
@TableName("student_cert")
public class StudentCert {
    //主键ID，自增
    @TableId(type = IdType.AUTO)
    private Long id;
    //关联用户ID
    private Long UserId;
    //学生姓名
    private String student_name;
    //学校
    private String school;
    //学号
    private Long studentId;
    //证件正面图片路径
    private String id_card_front;
    //证件反面图片路径
    private String id_card_back;
    //认证状态（0=待审核，1=审核通过，2=审核拒绝）
    private Byte status;
    //审核备注
    private String remark;
    //申请时间
    private LocalDateTime createTime;
}
