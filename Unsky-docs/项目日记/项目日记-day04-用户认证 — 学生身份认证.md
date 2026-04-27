> 日期：2026/04/26
> 目标：保证认证通过的学生可以发布商品进行交易

---

## 项目结构
---
## 一、验证登录与开发受保护接口

### 1.1 补充 `/api/user/info`原因

- 在day03的学习中，已经完成登录并返回Token
- day04需要先验证"带`Token`访问受保护接口"是否真的可用
- `/api/user/info` 是登录态落地的第一步

> 1. 什么是受保护接口？
> 关于接口，**受保护接口 = 必须先登录、带合法 Token 才能访问的接口**。可以把它理解成“先验证身份，再决定是否放行”的接口；而更进一步的“管理员接口”，则是在登录的基础上，还要继续判断当前用户是否具备管理员权限。

> 2. 什么是拦截器？  
> 拦截器可以理解为后端接口访问前的一道“关卡”。当前端请求到达 Controller 之前，拦截器会先对请求进行预处理，例如检查是否携带 Token、Token 是否有效，以及当前请求是否属于需要登录后才能访问的受保护接口。只有校验通过，请求才会继续进入后续业务逻辑；否则会被直接拦截并返回错误结果。

---
### 1.2 实际操作步骤

1. 首先需要在`UserController`中新增并声明 `/api/user/info`

```Java
@GetMapping("/info") //当前接口的作用是查询当前登录用户信息  
public Result<User> info(){  
    return Result.success(null);  
}
```

2. 在`UserService`接口中声明查询当前用户信息的方法

```Java
/**  
 * 在业务层进行声明，定义这个业务层的能力  
 * Service 关心什么？  
 * 只关心：你给我一个 userId，我就帮你查这个用户  
 */  
Result<User> info(Long userId); //根据当前登录用户 id 查询用户信息
```

3. 在`UserServiceImpl`接口实现类中实现当前用户信息查询的具体逻辑

```Java
@Override  //重写UserService接口里面的方法
public Result<User> info(Long userID) {  
    User user = userMapper.selectById(userID); //主键 id 已知，直接根据ID查询  
    if (user == null) {  
        return Result.error("用户不存在");  
    }  
    return Result.success(user); //暂时返回user，弊端是会直接返回password，后续会优化  
}
```

4. 在`UserController`中真正调用`userService.info(...)`

```Java
@GetMapping("/info") //当前接口的作用是查询当前登录用户信息  
public Result<User> info(){  
    //return Result.success(null); 注释掉原来使用的空壳方法  
    return userService.info(1L); //1L是一个临时测试写法，并不是最终写法  
}
```

> 说明：此处先临时写成 `return userService.info(1L);`，目的不是完成最终版接口，而是先验证 `/api/user/info` 的基础调用链路是否打通。后续会将 `1L` 替换为通过 Token 解析得到的真实当前用户 id。


5. 在`JwtUtil`中补充解析 Token 的方法，为后续将 `1L` 替换为真实当前登录用户 id 做准备

```Java
/**  
 * 根据token解析当前用户id  
 * 登录时是把 userId 放进 Token  
 * 现在是把 Token 中的 userId 再取出来并解析  
 * 这一步是后续接入受保护接口身份识别的基础  
 * @param token  
 * @return  
 */  
public static Long getUserIdFromToken(String token) {  
    Claims claims = Jwts.parserBuilder()  
            .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))  
            .build()  
            .parseClaimsJws(token)  
            // parserBuilder 会先校验这张 Token 的签名与格式是否有效
            .getBody();    
    return Long.valueOf(claims.getSubject());  
}
```


6. 在 `UserController` 中接收请求头中的 Token，成功替换原来固定id=1的用户

```Java
@GetMapping("/info") //当前接口的作用是查询当前登录用户信息  
/**  
 * 关于return userService.info(1L);当前写法仅用于打通基础调用链，后续会替为真实登录用户id  
 * 后面一定会改成：  
 * 通过 Token 解析当前用户 id  
 * 再传给 userService.info(userId)  
 * @RequestHeader("token")从请求头里取出名为 token 的值，在Apifox里测试时，就不能什么都不传了，而要在请求头里加token字符串  
 */  
public Result<User> info(@RequestHeader("token")  String token){  
    //return Result.success(null);注释掉原来使用的空壳方法  
    //return userService.info(1L);//1L是一个临时测试写法，并不是最终写法,已经调整  
    Long userId = JwtUtil.getUserIdFromToken(token);//调用解析方法，将解析内容传入userID  
    return userService.info(userId);  
}
```


--- 

### 1.3 测试结果展示

#### 1. 第一轮测试----初步检测完整链路是否跑通(1L固定用户id=1)

在完成 `/api/user/info` 的基础调用链路接通之后，先使用临时写死的 `1L` 进行接口联调测试，也就是对应上方 1.2 的第四步代码。当前阶段的目标不是立刻完成 Token 鉴权，而是先把“龙骨”跑通，再继续补“绿叶” (⁄˃ᴗ˂⁄)！

1. 本次测试目标不是 Token 鉴权，而是验证以下内容是否正常：

- 接口地址是否正确
- Controller 是否能够接收到请求
- Service 是否能够被正常调用
- 是否能够根据用户 id 查询到数据库中的用户信息

2. 测试方式：

- 请求方式：`GET`
- 请求地址：`/api/user/info`
- 当前阶段不需要传参数，也不需要携带 Token

3. 测试结果：

- 接口请求成功
- 成功返回数据库中 `id = 1` 的用户信息
- 说明 `/api/user/info` 的基础查询链路已经打通

4. 测试截图：(⁄˃ᴗ˂⁄)

![[Pasted image 20260426154010.png]]

5. 补充说明：

- 当前返回结果中仍然包含 `password` 字段
- 虽然这里返回的是加密后的密文，但从接口设计角度来看，后续仍需要进行脱敏处理
- 当前`1L`只是临时写法，body里不需要参数，后续还需要改为通过 Token 解析真实登录用户 id

#### 2. 第二轮测试----携带 Token 访问 `/api/user/info`：

在将 `/api/user/info` 从临时写死的 `1L` 升级为“通过请求头接收 Token 并解析真实 `userId`”之后，需要继续进行第二轮联调测试。

1. 本次测试目标：

- 验证请求头中的 Token 是否能够被后端正常接收
- 验证 `JwtUtil.getUserIdFromToken(token)` 是否能够正确解析用户 id
- 验证 `/api/user/info` 是否能够根据真实登录用户返回对应用户信息

2. 测试步骤：

  ①先调用登录接口 `/api/user/login`
  ② 从登录成功结果中复制返回的 `token`
  ③ 打开 `GET /api/user/info`
  ④ 在 `Headers` 中新增：`token: 登录接口返回的token值`
  ⑤点击发送请求

3. 测试结果：

- 成功返回当前登录用户信息
- 说明 Token 已经能够参与后续接口身份识别
- `/api/user/info` 已经从临时联调版本，升级为初步可用的受保护接口

4. 测试截图：(˶˃ ᵕ ˂˵)

- 登录"天下云"账号

![[Pasted image 20260427183101.png]]

- 复制token并调用info接口测试是否返回用户信息

![[Pasted image 20260427182945.png]]

5.  补充说明：

- 本轮测试与第一轮测试的区别在于：第一轮只是临时写死 `1L` 来验证基础查询链路是否打通，而第二轮则已经正式通过请求头接收 `Token`，并解析出真实当前登录用户的 `userId`
- 这说明 `/api/user/info` 已经不再是“固定查询某一个测试用户”的临时版本，而是开始具备“谁登录、谁访问、就返回谁自己的信息”这一层真实业务含义
- 当前这一步的核心意义，在于验证 Token 已经能够真正参与后续接口身份识别，为后面的学生身份认证申请、认证状态查询等功能打下基础
- 需要注意的是，虽然当前接口主链路已经跑通，但返回结果中仍然包含 `password` 字段，后续还需要继续进行脱敏优化，避免将密码相关信息暴露给前端

---
### 1.4 阶段小结

`/api/user/info` 接口属于 day04 中一个非常重要的过渡接口，它体现的是真实开发中“先声明接口、再补业务逻辑、最后联调测试”的完整过程。这个接口本身不是用来生成 Token 的，而是为了验证：**在 day03 已经完成登录并返回 Token 的前提下，系统后续是否具备根据当前用户身份查询个人信息的能力。**

在整个认证体系中，**Token 更像是一张带有效期、带身份标识的登录凭证**。它的核心作用不是直接展示给前端，而是让后端后续能够识别“当前访问接口的人是谁”。而 `/api/user/info` 正是后续承接这套登录态识别逻辑的重要接口。只是由于当前前端页面还没有编写，所以暂时先使用 Apifox 来完成接口联调测试。

本次测试第一轮和第二轮完美结束，已经正式接入“通过 Token 解析真实用户 id”这一步，也就是 `Controller → Service → ServiceImpl → Mapper → MySQL` 这条流程是成立的。

同时，这次测试也暴露出了一个后续需要继续优化的问题：接口返回结果中仍然直接包含了加密后的 `password` 字段。虽然这不是明文密码，但从接口安全和数据脱敏的角度来看，后续仍需要继续处理，避免将密码相关字段暴露给前端。

> 总体来看，我认为把这一部分作为 day04 的第一节是非常合理的，因为后续无论是学生身份认证申请、认证状态查询，还是管理员审核，本质上都要建立在“系统能够识别当前登录用户身份”这个前提之上，而这个前提正是由登录返回的 Token 和后续受保护接口共同支撑起来的。


### 1.5 后续优化逻辑(接口安全与数据脱敏)

1. 创建`UserInfoVO`来代替原来的`User`实体类并删掉password

```Java
@Data  
public class UserInfoVO {  
  
    private Long id;  
  
    private String nickname;  
  
    private String phone;  
  
    private String avatar;  
  
    private String school;  
  
    private String studentId;  
  
    private Byte authStatus;  
  
    private Integer creditScore;  
}
```

2. 在 `UserServiceImpl`中修改返回值类型为`Result<UserInfoVO>`并添加对应方法

```Java
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
```

3. 将`UserService`，`UserController`中的返回值改为`Result<UserInfoVO>`

```Java
Result<UserInfoVO> info(Long userId);
```

```Java
public Result<UserInfoVO> info(@RequestHeader("token")  String token){
	Long userId = JwtUtil.getUserIdFromToken(token);  
	return userService.info(userId);
```

4. 核心知识点与注意点
- `User` 更偏向数据库实体，`VO`可以理解成**专门给接口返回用的数据对象**
- `VO`核心作用是：控制返回字段、避免敏感信息泄露、让接口更清晰
- `VO`里的内容 通常来源于实体类中的部分字段，可以根据接口返回需求灵活设计
- 本质上讲就是修改返回值类型达到**我想传什么就传什么**的最终目的
 
---
## 二、学生认证数据建模

### 2.1. 在数据库中创建`student_cert` 表(使用 DataGrip 执行 SQL 脚本)


```sql  
DROP TABLE IF EXISTS student_cert;  
  
CREATE TABLE student_cert (  
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',  
    user_id        BIGINT       NOT NULL COMMENT '关联用户ID',  
    student_name   VARCHAR(50)  NOT NULL COMMENT '学生姓名',  
    school         VARCHAR(100) NOT NULL COMMENT '学校名称',  
    student_id     VARCHAR(50)  NOT NULL COMMENT '学号',  
    id_card_front  VARCHAR(255) NOT NULL COMMENT '证件正面图片路径',  
    id_card_back   VARCHAR(255) NOT NULL COMMENT '证件反面图片路径',  
    status         TINYINT      DEFAULT 0 COMMENT '认证状态（0=待审核，1=审核通过，2=审核拒绝）',  
    remark         VARCHAR(255) DEFAULT NULL COMMENT '审核备注',  
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间'  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生认证表';  
```

### 2.2 创建 `StudentCert.java` 实体类

- 在完成 `student_cert` 表建表之后，接下来需要在后端补充与该表对应的实体类 `StudentCert.java`。  
- 这一步的作用，是将数据库中的学生认证申请记录映射为 Java 中可操作的数据对象，为后续的认证申请提交、认证状态查询和管理员审核提供统一的数据载体。

```java 
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
```

核心理解：
- `student_cert` 对应 `StudentCert`
- 下划线字段通过 MyBatis-Plus 自动映射为驼峰字段
- 实体类是后续所有学生认证业务操作的数据基础

### 2.3创建`StudentCertMapper` 数据访问层接口

```Java
@Mapper  
public interface StudentCertMapper extends BaseMapper<StudentCert> {  
}
```

### 2.4 创建 `StudentCertService` 业务接口

- 在完成`StudentCertMapper`之后，继续补充学生认证模块的业务层接口`StudentCertService`。这一步的作用，是先把学生认证业务的 Service 骨架搭出来，为后续提交认证申请、查询认证状态和管理员审核等逻辑预留业务入口。

```Java
public interface StudentCertService {}
```

### 2.5 创建`StudentCertServiceImpl` 业务接口实现类

- 在完成 `StudentCertService` 接口之后，继续补充 `StudentCertServiceImpl` 作为学生认证模块的业务实现层。后续关于认证申请提交、认证状态查询和管理员审核等逻辑，都将在这一层中正式实现。

```Java
/**  
 * 它的作用是：  
 * 真正承接后面学生认证相关业务逻辑  
 * 比如提交认证申请、查询认证状态、管理员审核等  
 */  
@Service  
public class StudentCertServiceImpl implements StudentCertService {  
  
    @Autowired  
    private StudentCertMapper studentCertMapper;  
  
}
```

核心理解：

- `StudentCertService` 用于声明业务能力
- `StudentCertServiceImpl` 用于真正实现业务逻辑
- 提前注入 `StudentCertMapper`，是为了给后续学生认证表的数据操作做好准备


---

## 三、学生认证业务实现 ˗ˏˋ ★ ˎˊ˗

> 接口目标：登录用户提交学校、学号、证件照片等认证资料

### 3.1 提交认证申请接口

1. 在 `StudentCertService` 中提交认证申请接口

- 在完成学生认证模块基础骨架后，开始第一个真实业务：提交认证申请。为了保持`Controller → Service → ServiceImpl → Mapper`的分层结构，首先需在`StudentCertService`中声明“提交认证申请”方法，为后续具体实现预留业务入口。

```Java
/**  
 * 用户提交学生认证信息  
 * @param userId  
 * @param studentCert  
 * @return  
 */  
Result<Void> submitCert(Long userId, StudentCert studentCert);
```

核心理解：

- `userId` 用于表示当前登录用户身份
- `studentCert` 用于承接前端提交的认证资料
- 当前阶段先定义业务能力，下一步再进入具体实现

2. 在 `StudentCertServiceImpl` 中实现提交认证申请逻辑

- 在 `StudentCertService` 中声明提交认证申请方法之后，接下来需要在 `StudentCertServiceImpl` 中正式实现这一业务逻辑。当前这一阶段的核心目标，是先完成最基础的认证申请入库流程。

```Java
@Override  
/**  
 * 提交学生认证申请  
 *核心流程：  
 *  * 1. 校验是否已提交认证（防重复提交）  
 *  * 2. 构造认证数据（绑定当前用户 + 初始化状态）  
 *  * 3. 插入数据库  
 */  
public Result<Void> submitCert(Long userId, StudentCert studentCert) {  
    //构造数据 强制绑定当前登录用户（防止前端伪造 userId）  
    studentCert.setUserId(userId);  
    //初始化认证状态：0 = 待审核  
    studentCert.setStatus((byte) 0);  
    //插入数据库  
    int rows = studentCertMapper.insert(studentCert);  
    // 插入失败（理论上很少发生，但必须兜底）  
    if (rows <= 0) {//  
        return Result.error("认证申请提交失败");  
    }  
    return Result.success(null);  
}
```

核心理解：

- `userId` 来自当前登录用户身份，而不是前端随意传入
- 提交申请时需要先补全 `userId` 和初始认证状态
- 通过“先查询再插入”的方式，在业务层实现**防重复提交控制**，并在插入时强制绑定当前用户ID与初始化认证状态，保证数据安全与状态正确流转。
- 当前阶段先完成最基础的“认证申请写入数据库”能力，后续再继续补充**重复提交校验**、**状态判断**等增强逻辑

3. 在`StudentCertController` 层声明“提交认证申请”接口


### 3.2 












---
## 四、
---
## 五、今日成果总结
---
## 六、下一步任务(day05)
---
## 七、踩坑记录
---
## 八、我话很多：(⁄ ⁄•⁄ω⁄•⁄ ⁄)(2026/04/25)
