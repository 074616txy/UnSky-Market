package com.Market.user.service;


import com.Market.common.Result;
import com.Market.common.entity.User;

/**
 * 本质上写接口是定义规则
 * 作用：
 * 1. 解耦（核心），不用动Controller，它只依赖接口
 * 2. 可替换 ，可以写出多个接口来随时替换
 * 3. 规范 ，在团队开发中，接口 = “合同”
 */
public interface UserService {

    Result<Void> register(User user);

    /**
     * 这一层主要是系统必须提供一个登录功能，输入 User，返回 Result<User>
     * login方法必须与UserController里面的方法对应
     * @param user
     * @return
     */
    Result<User> login(User user);

}