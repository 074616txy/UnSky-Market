package com.Market.cert.controller;


import com.Market.cert.service.StudentCertService;
import com.Market.cert.vo.StudentCertVO;
import com.Market.common.entity.StudentCert;
import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cert")
public class StudentCertController {

    private final StudentCertService studentCertService;

    public StudentCertController(StudentCertService studentCertService) {
        this.studentCertService = studentCertService;
    }

    /**
     * 后端链路打通测试，测试Controller → Service → Mapper → DB
     * 测试接口：根据用户ID查询认证信息
     */
    @GetMapping("/test")
    public StudentCert test(@RequestParam Long userId) {
        return studentCertService.getByUserId(userId);
    }

    /**
     * 提交认证申请的接口，返回token和studentCert
     * @param token
     * @param studentCert
     * @return
     */
    @PostMapping("/submit")//提交一条新的认证申请记录，本质上是新增数据，所以用@PostMapping
    public Result<Void> submit(@RequestHeader("token") String token,@RequestBody StudentCert studentCert) {

        Long userId = JwtUtil.getUserIdFromToken(token);
        return studentCertService.submitCert(userId, studentCert);
    }

    /**
     * 具体逻辑：先从请求头拿到 Token，对token进行解析取得userId，根据这个userId获取认证状态
     * 直接返回Result<StudentCert>，查询当前用户在 student_cert 表中的那条认证记录
     * @param token
     * @return
     */
    @GetMapping("/status")
    public Result<StudentCertVO> GetCertStatus(@RequestHeader("token") String token) {

        Long userId = JwtUtil.getUserIdFromToken(token);
        return studentCertService.getCertStatus(userId);
    }

    /**
     * 接口默认管理员操作
     * 审核动作本质上是：修改认证申请状态，所以用@PostMapping
     * @param id
     * @param status
     * @param remark
     * @return
     */
    @PostMapping("/audit")
    public Result<Void> auditCert(@RequestParam Long id,
                                  @RequestParam Byte status,
                                  @RequestParam(required = false) String remark) {
        return studentCertService.auditCert(id, status, remark);
    }

}


