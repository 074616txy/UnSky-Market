package com.Market.user.service.impl;


import com.Market.common.result.Result;
import com.Market.common.entity.User;
import com.Market.common.util.JwtUtil;
import com.Market.user.mapper.UserMapper;
import com.Market.user.service.UserService;
import com.Market.user.vo.LoginVO;
import com.Market.user.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 * 接口实现类的本质就是实现规则，ServiceImpl = “把请求变成数据库操作，再变成结果返回”
 * 真正处理业务逻辑
 * 主要步骤：① 接收参数 ② 构造查询条件 ③ 查数据库 ④ 返回结果
 * 我要登录网站，把我写的数据传进来，在这里进行操作修改拼装，组成数据库格式，在数据库查找是否有信息，最后返回结果
 */
@Service//Spring扫描到这个注解，就回去service包里面找
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;//私有化一个对象useMapper来方便后面使用UserMapper接口

    //创建一个密码加密工具passwordEncoder，对密码进行加密
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    //先查手机号是否已存在
    public Result<Void> register(User user) {

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",user.getPhone());

        //用一个变量接收查询结果
        User result = userMapper.selectOne(wrapper);

        /**
         *用户重复注册的判断（绿叶补强）
         */
        if(result != null){
            return Result.error("该手机号已被注册");
        }

        /**
         * 密码加密逻辑（绿叶补强）
         * 把用户传进来的明文密码加密，再塞回 user 对象里
         */
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        /**
         * 这里通过userMapper来使用UserMapper接口里的insert方法
         * 从而实现将注册信息插入数据库的操作
         */
        userMapper.insert(user);

        return Result.success();
    }

    @Override
    public Result<LoginVO> login(User user) {

        /**
         * 创建一个“查询条件容器”QueryWrapper= 帮你拼 SQL 的工具，eq() = 等于条件（=）
         * 数据库里必须同时存在这个用户名 + 密码，才算登录成功
         * 这段代码 = 查用户是否存在
         * 根据大纲内容，要求用手机号+密码登录
         */
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", user.getPhone());
        //这里要做密码加密的内容，不能按照原明文密码进行查找登录了
        //wrapper.eq("password", user.getPassword());

        //这里是登陆的本质逻辑，去数据库找一个同时满足手机号 + 密码的用户
        User result = userMapper.selectOne(wrapper);

        //加密密码配对----passwordEncoder.matches(user.getPassword(), result.getPassword())
        if (result != null && passwordEncoder.matches(user.getPassword(), result.getPassword())) {
            String token = JwtUtil.generateToken(result.getId());//先生成身份认证
            LoginVO loginVO = new LoginVO();
            loginVO.setUser(result);
            loginVO.setToken(token);
            return Result.success(loginVO);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
    @Override
    public Result<UserInfoVO> info(Long userID) {
        User user = userMapper.selectById(userID);//主键 id 已知,直接根据ID查询
        if (user == null) {
            return Result.error("用户不存在");
        }
        /**
         * 关于返回json中带有password的优化：
         * 创建一个UserInfoVO封装除密码外的所有数据
         * 将返回值的类型变为UserInfoVO
         */
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setSchool(user.getSchool());
        userInfoVO.setStudentId(user.getStudentId());
        userInfoVO.setAuthStatus(user.getAuthStatus());
        userInfoVO.setCreditScore(user.getCreditScore());

        return Result.success(userInfoVO);//暂时返回user，弊端是会直接返回password，后续会优化----已优化
    }
}