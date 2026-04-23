package com.Market.user.service.impl;


import com.Market.common.Result;
import com.Market.common.entity.User;
import com.Market.user.mapper.UserMapper;
import com.Market.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

/**
 * 接口实现类的本质就是实现规则，ServiceImpl = “把请求变成数据库操作，再变成结果返回”
 * 真正处理业务逻辑
 * 主要步骤：① 接收参数 ② 构造查询条件 ③ 查数据库 ④ 返回结果
 * 我要登录网站，把我写的数据传进来，在这里进行操作修改拼装，组成数据库格式，在数据库查找是否有信息，最后返回结果
 */
@Service//Spring扫描到这个注解，就回去service包里面找
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<Void> register(User user) {

        userMapper.insert(user);

        return Result.success();
    }

    @Override
    public Result<User> login(User user) {

        /**
         * 创建一个“查询条件容器”QueryWrapper= 帮你拼 SQL 的工具，eq() = 等于条件（=）
         * 数据库里必须同时存在这个用户名 + 密码，才算登录成功
         * 这段代码 = 查用户是否存在
         * 根据大纲内容，要求用手机号+密码登录
         */
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", user.getPhone());
        wrapper.eq("password", user.getPassword());

        //这里是登陆的本质逻辑，去数据库找一个同时满足手机号 + 密码的用户
        User result = userMapper.selectOne(wrapper);

        if (result != null) {
            return Result.success(result);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
}