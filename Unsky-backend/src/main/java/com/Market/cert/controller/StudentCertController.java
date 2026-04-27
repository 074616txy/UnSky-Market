package com.Market.cert.controller;


import com.Market.cert.service.StudentCertService;
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

    @PostMapping("/submit")
    public Result<Void> submit(@RequestHeader("token") String token,@RequestBody StudentCert studentCert) {

        Long userId = JwtUtil.getUserIdFromToken(token);
        return studentCertService.submitCert(userId, studentCert);
    }
}


