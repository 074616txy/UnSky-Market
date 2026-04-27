package com.Market.user.controller;


import com.Market.common.result.Result;
import com.Market.common.entity.User;
import com.Market.common.util.JwtUtil;
import com.Market.user.mapper.UserMapper;
import com.Market.user.service.UserService;
import com.Market.user.vo.LoginVO;
import com.Market.user.vo.UserInfoVO;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController //让这个类可以接受HTTP请求的
@RequestMapping("/api/user")//让这个类有统一路径注册→/user/register 登录 → /user/login
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

    @GetMapping("/info") //当前接口的作用是查询当前登录用户信息
    /**
     * 关于return userService.info(1L);当前写法仅用于打通基础调用链，后续会替换为真实登录用户 id
     * 后面一定会改成：
     * 通过 Token 解析当前用户 id
     * 再传给 userService.info(userId)
     * @RequestHeader("token")从请求头里取出名为 token 的值，在Apifox里测试时，就不能什么都不传了，而要在请求头里加token字符串
     */
    public Result<UserInfoVO> info(@RequestHeader("token")  String token){
        //return Result.success(null);注释掉原来使用的空壳方法
        //return userService.info(1L);//1L是一个临时测试写法，并不是最终写法,已经调整
        Long userId = JwtUtil.getUserIdFromToken(token);//调用解析方法，将解析内容传入userID
        return userService.info(userId);
    }
}
