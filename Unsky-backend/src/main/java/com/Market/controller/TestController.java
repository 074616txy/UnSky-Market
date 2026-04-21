package com.Market.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器：验证所有接口通路是否正常
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String testhttp() {
        return "Upon the bow, I gaze into the distance.";
    }
}
