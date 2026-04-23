package com.Market.user.controller;

import com.Market.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 测试控制器：验证所有接口通路是否正常
 */
@RestController
public class TestController {
    /**
     * 测试后端通过http是否与前端页面连通
     * @return
     */
    @GetMapping("/test")
    public String testHTTP() {
        //如果连通成功，前端将会展示 HTTP测试成功.
        return "HTTP测试成功";
    }

    /**
     * 测试链路数据库 → 后端 → 前端是否跑通
     * 结果：成功在控制台和前端页面查询到数据的的全表
     * @return
     */
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/dbtest")
    public List testDB() {

        // 查询数据库
        List users = userMapper.selectList(null);

        // 打印到控制台
        System.out.println(users);

        //返回前端json格式
        return users;
    }
}