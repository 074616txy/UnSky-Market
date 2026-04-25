package com.Market.user.controller;


import com.Market.common.result.Result;
import com.Market.common.entity.User;
import com.Market.user.mapper.UserMapper;
import com.Market.user.service.UserService;
import com.Market.user.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController //让这个类可以接受HTTP请求的
@RequestMapping("api/user")//让这个类有统一路径注册→/user/register 登录 → /user/login
public class UserController {

    @Autowired
    //把 UserMapper 注入进来后可以直接操作数据库，但当前更推荐通过 Service 层做业务转发
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @PostMapping("/register")//路径注册→/user/register
    public Result<Void> register(@RequestBody User user){
        //@RequestBody->把前端传来的 JSON 数据 → 自动转换成 User 对象
        return userService.register(user);
    }

    @PostMapping("/login")//路径登录 → /user/login
    /**为社么这里返回类型是Result<User>而不是User？
     * 因为这里的返回内容是json格式的，里面会包含code,data,msg
     * 所以这个返回值是跟apifox里面的返回参数对应的
     * 这里的Result其实就是公共模块common里的类，那里面声明了所有返回类型的组合
     * Result<User> = 返回一个“包装好的 User 数据”
     */
    public Result<LoginVO> login(@RequestBody User user){
       return userService.login(user);
    }
}
