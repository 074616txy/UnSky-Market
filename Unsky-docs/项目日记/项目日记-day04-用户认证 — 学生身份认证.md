
> 日期：2026/04/26----04/28
> 目标：保证认证通过的学生可以发布商品进行交易

---

## 项目结构

- **这里只展示新增加的软件包和Java类，其余重复的内容用 /* 省略，只展现出更新后的结构**

```
D:\Develop\UnSky Market Project\
├── Unsky-backend/                      ← ★ 主业务模块
│   └── src/main/
│       └── java/com.Market
│           ├── cert                     // ★ 学生认证模块（Day04新增）
│           │   ├── controller
│           │   │   └── StudentCertController //  提交认证/查询状态/管理员审核
│           │   ├── mapper
│           │   │   └── StudentCertMapper
│           │   ├── service
│           │   │   ├── StudentCertService
│           │   │   └── impl
│           │   │       └── StudentCertServiceImpl
│           │   └── vo
│           │       └── StudentCertVO            // ★ 查询返回优化（状态描述）
│           │ 
│           └──user/*
│               └── vo // ★（day04补充）  
│                   └── UserInfoVO         // ★ 优化逻辑
└── Unsky-common/*                       ← ★ 公共模块（实体类归属）
    └── com.Market.common/*
        └── entity
            ├── StudentCert             // ★ 认证实体（实际在这里）
            └── User
```
---
## 一、验证登录与开发受保护接口

### 1.1 补充 `/api/user/info`原因

- 在day03的学习中，已经完成登录并返回Token
- day04需要先验证"带`Token`访问受保护接口"是否真的可用
- `/api/user/info` 是登录态落地的第一步

> 补充说明：  
> - 受保护接口：必须携带合法 Token 才能访问  
> - 拦截器：在请求进入 Controller 前进行统一校验（如 Token 校验）

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
 * 根据 userId 查询用户信息
 */  
Result<User> info(Long userId);
```

3. 在`UserServiceImpl`接口实现类中实现当前用户信息查询的具体逻辑

```Java
@Override  /
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
@GetMapping("/info")   
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
@GetMapping("/info")  
/**  
 * 关于return userService.info(1L);当前写法仅用于打通基础调用链，后续会替为真实登录用户id  
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

 1. 第一轮测试----初步检测完整链路是否跑通(1L固定用户id=1)

在完成 `/api/user/info` 的基础调用链路接通之后，先使用临时写死的 `1L` 进行接口联调测试，也就是对应上方 1.2 的第四步代码。当前阶段的目标不是立刻完成 Token 鉴权，而是先把“龙骨”跑通，再继续补“绿叶” (⁄˃ᴗ˂⁄)！

![[Pasted image 20260426154010.png]]

 补充说明：

- 当前返回结果中仍然包含 `password` 字段
- 虽然这里返回的是加密后的密文，但从接口设计角度来看，后续仍需要进行脱敏处理
- 当前`1L`只是临时写法，body里不需要参数，后续还需要改为通过 Token 解析真实登录用户 id

 2. 第二轮测试----携带 Token 访问 `/api/user/info`：

在将 `/api/user/info` 从临时写死的 `1L` 升级为“通过请求头接收 Token 并解析真实 `userId`”之后，需要继续进行第二轮联调测试。

- 登录"天下云"账号

![[Pasted image 20260427183101.png]]

- 复制token并调用info接口测试是否返回用户信息

![[Pasted image 20260427182945.png]]

  补充说明：
- 这说明 `/api/user/info` 已经不再是“固定查询某一个测试用户”的临时版本，而是开始具备“谁登录、谁访问、就返回谁自己的信息”这一层真实业务含义
- 当前这一步的核心意义，在于验证 Token 已经能够真正参与后续接口身份识别，为后面的学生身份认证申请、认证状态查询等功能打下基础
- 需要注意的是，虽然当前接口主链路已经跑通，但返回结果中仍然包含 `password` 字段，后续还需要继续进行脱敏优化，避免将密码相关信息暴露给前端

---
### 1.4 阶段小结

本阶段完成了以下核心能力：  
  
1. 打通 /api/user/info 基础链路（Controller → Service → DB）  
2. 接入 Token 并实现当前用户身份识别  
3. 初步形成受保护接口访问模式  
  
当前系统已经具备：  
- 登录态识别能力  
- 基于用户身份的数据查询能力  
  
遗留问题：  
- 接口返回仍包含 password 字段（已在后续优化中解决）

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
-  `VO` 用于控制接口返回字段  
-  **避免敏感信息泄露（如 `password`）**  
-  根据接口需求灵活定义返回结构
-  本质上讲就是修改返回值类型达到**我想传什么就传什么**的最终目的
 
---

## 二、学生认证数据建模

### 2.1. 在数据库中创建`student_cert` 表(使用 DataGrip 执行 SQL 脚本)


```mysql  
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
    private Long userId;  
    //学生姓名  
    private String studentName;  
    //学校  
    private String school;  
    //学号  
    private String studentId;  
    //证件正面图片路径  
    private String idCardFront;  
    //证件反面图片路径  
    private String idCardBack;  
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
- 驼峰映射仅在字段被 ORM 正确识别时生效，字段未参与 SQL 构建时不会自动映射（踩坑点）

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
    /**  
     * 为什么在接口实现类和控制类都要写下面这个而不是@Autowired private StudentCertMapper studentCertMapper;？  
     * - 前者是构造器的注入，后者是字段的注入；前者更加安全，不会出现null，后面报错率高  
     */  
    private final StudentCertMapper studentCertMapper;
  
}
```

核心理解：

- `StudentCertService` 用于声明业务能力
- `StudentCertServiceImpl` 用于真正实现业务逻辑
- 提前注入构造器`StudentCertMapper`，是为了给后续学生认证表的数据操作做好准备


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

2. 在 `StudentCertServiceImpl` 中实现提交认证申请逻辑

- 在 `StudentCertServiceImpl` 中实现提交认证申请逻辑，在完成基础入库的同时，补充用户绑定、状态初始化及防重复提交等核心约束。

```Java
@Override
/**
 * 提交学生认证申请
 */
@Transactional // ⭐事务：保证“校验 + 插入”原子性（要么全成功，要么全失败）
public Result<Void> submitCert(Long userId, StudentCert studentCert) {

    // ⭐防重复提交：同一用户只能有一条“待审核”记录
    LambdaQueryWrapper<StudentCert> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(StudentCert::getUserId, userId)
           .eq(StudentCert::getStatus, 0); // 0 = 待审核

    Long count = studentCertMapper.selectCount(wrapper);
    if (count > 0) {
        return Result.error("请勿重复提交认证申请");
    }

    // ⭐绑定当前用户（禁止前端传 userId，防伪造）
    studentCert.setUserId(userId);

    // ⭐初始化状态：0 = 待审核（状态流转起点）
    studentCert.setStatus((byte) 0);

    // 插入数据库
    int rows = studentCertMapper.insert(studentCert);

    // ⭐兜底：插入失败（极少，但必须处理）
    if (rows <= 0) {
        return Result.error("认证申请提交失败");
    }

    return Result.success(null);
}
```

3. 在`StudentCertController` 层声明“提交认证申请”接口

- 在完成 `StudentCertServiceImpl` 中的提交认证申请业务逻辑之后，接下来需要在 `StudentCertController` 中对外暴露接口，使前端或 Apifox 可以真正发起认证申请请求。

```Java
/**  
 * 提交认证申请的接口，返回token和studentCert  
 * 注意：userId必须由后端解析 Token 得到，而不是前端直接传入
 * @param token  
 * @param studentCert  
 * @return  
 */  
@PostMapping("/submit")//提交一条新的认证申请记录，本质上是新增数据，所以用@PostMapping  
public Result<Void> submit(@RequestHeader("token") String token,@RequestBody StudentCert studentCert) {  
  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    return studentCertService.submitCert(userId, studentCert);  
}
```

4. 在`Apifox`里面进行提交认证申请接口测试与验证

- 在完成 `StudentCertController`、`StudentCertService` 和 `StudentCertServiceImpl` 之后，接下来使用 Apifox 对提交认证申请接口进行联调测试，验证“登录身份识别 + 认证申请入库”这条链路是否能够正常运行。

- 测试Body：

```json
{
  "studentName": "Skyron",
  "school": "bilibili大学",
  "studentId": "20230001",
  "idCardFront": "test-front.png",
  "idCardBack": "test-back.png",
  "remark": "day04 测试认证申请"
}
```

- 测试结果说明：

> 1. 首次提交：成功写入数据库，认证状态为“待审核”
> 2. 重复提交：接口返回“请勿重复提交认证申请”，未新增数据(不做截图展示)

- 测试截图：(首次提交)

![[Pasted image 20260428222259.png]]

- 数据库结果截图：(首次提交)

![[Pasted image 20260428221728.png]]

- 补充说明：

> 1. 当前已通过 Service 层实现防重复提交校验，避免同一用户生成多条“待审核”记录
> 2. 数据约束由业务层控制，而非数据库唯一索引实现
> 3. 当前版本在提交认证阶段已增加防重复提交约束，在审核阶段已实现状态同步更新，认证模块核心业务链路已具备完整性与一致性

> 至此，学生认证模块从“基础功能实现”升级为“具备业务约束与数据一致性的可用模块”

### 3.2 实现查询认证状态接口

1. 在 `StudentCertService` 中声明查询认证状态方法

- 在完成“提交认证申请”接口之后，接下来需要继续补充“查询认证状态”这一能力。为了保持 `Controller → Service → ServiceImpl → Mapper` 的分层结构，首先需要在 `StudentCertService` 中声明根据当前登录用户 id 查询认证状态的方法。

```Java
/**  
 * 用户查询自己的认证信息  
 * @param userId  
 * @return  
 */  
Result<StudentCert> getCertStatus(Long userId);
```

2. 在 `StudentCertServiceImpl` 中实现查询认证状态逻辑

```Java
/**  
 * 根据当前用户id查询学生认证状态  
 */  
@Override  
public Result<StudentCert> getCertStatus(Long userId) {  
    LambdaQueryWrapper<StudentCert> wrapper = new LambdaQueryWrapper<>(); 
    // ⚠️ 这里不能使用 selectById，因为查询条件是 user_id（用户维度），不是主键 id 
    wrapper.eq(StudentCert::getUserId, userId);  
  
    StudentCert studentCert = studentCertMapper.selectOne(wrapper); 
    // ⚠️ 当前使用 selectOne 基于“一个用户只有一条认证记录”的业务假设 
    // 如果存在重复提交（多条记录），这里会抛出异常（TooManyResultsException）后续完善
    if (studentCert == null) { // 用户还未提交认证申请 
        return Result.error("当前用户暂无认证申请记录");  
    }  
    return Result.success(studentCert);  
}
```

3. 在 `StudentCertController` 中声明查询认证状态接口

- 在完成 `StudentCertServiceImpl` 中的查询认证状态逻辑之后，接下来需要在 `StudentCertController` 中继续暴露对应接口，使前端或 Apifox 可以根据当前登录身份查询自己的认证状态。

```Java
/**  
 * 具体逻辑：先从请求头拿到 Token，对token进行解析取得userId，根据这个userId获取认证状态  
 * 直接返回Result<StudentCert>，查询当前用户在 student_cert 表中的那条认证记录  
 * @param token  
 * @return  
 */  
@GetMapping("/status")  
public Result<StudentCert> GetCertStatus(@RequestHeader("token") String token) {  
  
  // 通过 Token 解析当前用户身份（userId 不允许前端传入）
    Long userId = JwtUtil.getUserIdFromToken(token);  
    return studentCertService.getCertStatus(userId);  
}
```

4. 在`Apifox`里面进行查询认证状态接口测试与验证

- 在完成 `StudentCertController` 中查询认证状态接口之后，接下来使用 Apifox 对 `/api/cert/status` 进行联调测试，验证当前登录用户是否能够根据请求头中的 Token 查询到自己的认证状态

![[Pasted image 20260428150625.png]]

>  接口测试通过，已验证“Token → userId → 查询认证状态”链路正常

- 补充说明：

> 1. 在前面的测试过程中，如果同一用户重复提交认证申请，而后端又没有做重复提交校验，就可能导致 `student_cert` 表中同一个 `user_id` 出现多条记录。此时，当前使用 `selectOne()` 的查询逻辑就会报出“查询结果超过一条”的错误。
> 2. 在后续补强中，需要继续为“提交认证申请”接口增加重复提交校验，避免同一用户反复插入多条认证记录。当前阶段为了保持测试账号统一，已经通过清理重复数据并保留唯一记录的方式，先将主链路测试顺利跑通。

5. 扩展：查询接口返回优化----`StudentCertVO`

- 在当前实现中，查询认证状态接口直接返回 `StudentCert` 实体类，用于快速打通主链路。  
在实际项目中，通常会使用 VO（View Object）对返回数据进行封装，以控制字段暴露范围。  
本项目中已创建 `StudentCertVO`，用于后续接口优化：

```Java
@Data  //StudentCertVO代码展示
public class StudentCertVO {  
     private String studentName;  
     private String school;  
     private String studentId;  
  
     private String idCardFront;  
     private String idCardBack;  
  
     private Byte status;  
     private String statusDesc; // ⭐状态中文描述  
  
    private String remark;  
    }
```

```Java
//在StudentCertServiceImpl的getCertStatus方法中，将原返回StudentCert的逻辑替换为VO返回：
    //⭐转换VO  
    StudentCertVO studentCertVO = new StudentCertVO();  
    //⭐1. 自动拷贝基础字段  
    BeanUtils.copyProperties(studentCert, studentCertVO);  
  
    // ⭐2. 手动补充业务字段  
    Byte status = studentCert.getStatus();  
    if (status == 0) {  
    studentCertVO.setStatusDesc("待审核");  
    } else if (status == 1) {  
    studentCertVO.setStatusDesc("已通过");  
    } else if (status == 2) {  
    studentCertVO.setStatusDesc("已拒绝");  
    }
	// ⭐统一返回（必须最后）  
    return Result.success(studentCertVO);
```

>  在引入 VO 后，需要统一调整查询接口的返回类型（Service / ServiceImpl / Controller），由 `Result<StudentCert>` 修改为 `Result<StudentCertVO>`。

- 通过 VO 封装返回数据，实现“字段控制 + 状态语义补充”，避免直接暴露数据库实体。

### 3.3 管理员审核接口

1. 先在 `StudentCertService` 中声明管理员审核方法

- 在完成“提交认证申请”和“查询认证状态”之后，接下来需要继续补充管理员审核能力。为了保持 `Controller → Service → ServiceImpl → Mapper` 的分层结构，首先需要在 `StudentCertService` 中声明管理员审核认证申请的方法，为后续具体实现预留业务入口

```Java
/**  
     * 管理员审核学生认证申请  
     * 审核的是某一条具体的认证申请记录----学生认证表自己的主键id  
     * @param id  student_cert 表主键 id  
     * @param status  审核状态（1=通过，2=拒绝）  
     * @param remark  审核备注  
     * @return  
     */  
    Result<Void> auditCert(Long id, Byte status, String remark);  
}
```

2. 在 `StudentCertServiceImpl` 中实现管理员审核逻辑

- 在 `StudentCertService` 中声明管理员审核方法之后，接下来需要在 `StudentCertServiceImpl` 中正式实现这一业务逻辑。当前阶段的核心目标，是根据认证申请记录 `id` 更新审核状态与备注，并同步更新对应用户的 `authStatus`

```Java
/**  
 * 管理员审核学生认证信息  
 */  
@Override  
public Result<Void> auditCert(Long id, Byte status, String remark) {  
  
    StudentCert studentCert = studentCertMapper.selectById(id);  
    if (studentCert == null) {  
        return Result.error("认证申请记录不存在");  
    }  
        // 1. 更新认证申请状态和备注  
        studentCert.setStatus(status);  
        studentCert.setRemark(remark);  
        // ⚠️ 审核操作不仅影响认证记录，还会影响用户权限（后续发布商品依赖该状态）
        int certRows = studentCertMapper.updateById(studentCert);  
        if (certRows <= 0) {  
            return Result.error("认证审核失败");  
        }  
  
        // 2. 同步更新用户认证状态  
        User user = userMapper.selectById(studentCert.getUserId());  
        if (user == null) {  
            return Result.error("关联用户不存在");  
        }  
        // ⚠️ 必须同步更新用户表 authStatus，否则会导致用户状态与认证记录不一致
        user.setAuthStatus(status);
        // 后续“商品发布权限”将直接依赖该字段判断
        
        int userRows = userMapper.updateById(user);  
        if (userRows <= 0) {  
            return Result.error("用户认证状态更新失败");  
        }  
    return Result.success(null);  
}
```

3. 在 `StudentCertController` 中声明管理员审核接口

- 在完成 `StudentCertServiceImpl` 中的管理员审核逻辑之后，接下来需要在 `StudentCertController` 中继续暴露对应接口，使后端能够对指定认证申请记录进行审核处理。

```Java
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
```

4. 在`Apifox` 里进行管理员审核接口的测试与验证

- 在完成 `StudentCertController` 中管理员审核接口之后，接下来使用 Apifox 对 `/api/cert/audit` 进行联调测试，验证管理员是否能够对指定认证申请记录完成审核，并同步更新用户认证状态。

![[Pasted image 20260428222649.png]]


> 经过多次测试验证：`student_cert` 表中的认证状态和审核备注更新成功并且`sys_user` 表中的 `auth_status` 同步更新成功，关于数据库表的更新这里不做截图展示与过多赘述(˶˃ ᵕ ˂˵)

- 补充说明：

1. 当前审核接口主要用于验证认证主链路是否闭环，因此暂未继续补充管理员身份校验逻辑
2. 后续如需增强接口安全性，还可以进一步接入管理员登录态与角色权限校验

### 3.4 权限控制(day06补充)

在完成学生认证主链路之后，系统已经具备“识别当前用户身份 + 获取认证状态”的能力。  
  
这为后续业务中的权限控制提供了基础，例如：  
  
- 未认证用户是否允许发布商品？  
- 是否需要区分普通用户与管理员？  
  
👉 在后续 day06 商品发布功能中，将基于当前认证状态（authStatus）实现权限校验逻辑。  
  
> 详见：day06 商品发布接口设计（待实现）

---

## 四、核心业务约束（系统设计）

在完成学生认证主链路之后，需要对系统中的关键业务规则进行统一约束说明。

这些约束不依赖具体接口实现，而是贯穿整个系统的数据设计与业务流程，是后续功能扩展与权限控制的基础。

### 4.1 用户身份约束

- 所有受保护接口必须通过 Token 识别当前用户
- userId 必须由后端解析得到，不允许由前端传入
- 接口层只接收 Token，不直接信任任何用户标识参数

👉 目的：防止用户伪造身份，保证接口安全性

### 4.2 认证状态流转约束

系统中涉及两个状态字段：

- student_cert.status（认证申请状态）
- user.authStatus（用户认证状态）

状态流转规则如下：

| 阶段 | student_cert.status | user.authStatus |
|------|--------------------|-----------------|
| 初始 | -                  | 0（未认证）     |
| 提交申请 | 0（待审核）      | 0               |
| 审核通过 | 1                | 1               |
| 审核拒绝 | 2                | 2               |

👉 约束：两个状态必须保持一致

### 4.3 数据一致性约束（⭐核心）

- 管理员审核时，必须同时更新：
  - student_cert.status
  - user.authStatus

- 不允许出现以下情况：
  - 认证记录为“通过”，但用户仍是“未认证”
  - 用户状态已更新，但认证记录未更新

👉 实现方式：
- 在同一业务逻辑中完成双表更新（已实现✓）
- 后续可考虑使用事务（@Transactional）保证原子性（已实现✓）

### 4.4 唯一性与重复提交约束（⭐重点）

从业务角度：

- 一个用户在同一时间只能存在一条有效认证申请
- 不允许在“待审核”状态下重复提交

当前存在的问题：

- 数据库层面未限制 user_id 唯一性
- Service 层暂未做重复提交校验
- 可能导致 selectOne 查询异常（多条记录）

👉 推荐实现方案（后续补强）：

1. **业务层校验（推荐）**
   - 提交前查询当前用户是否已有认证记录
   - 若存在且状态为“待审核”，则拒绝提交

2. **数据库约束（可选）**
   - 可增加唯一索引（user_id）
   - 或结合状态字段设计复合唯一索引

👉 当前阶段选择：先通过业务层控制（更灵活）

### 4.5 查询约束

- 查询认证状态时，使用 user_id 作为条件，而不是主键 id
- 当前使用 selectOne()，依赖“单用户单记录”前提

👉 风险：
- 若存在多条记录，将抛出异常

👉 解决方向：
- 配合“重复提交约束”保证数据唯一性

### 4.6 权限前置约束（承接 day06）

在后续商品发布功能中，将引入权限控制规则：

- 商品发布接口必须为受保护接口
- 必须通过 Token 获取当前用户
- 必须校验 user.authStatus == 1（已认证）

👉 未认证用户禁止发布商品

👉 本约束将在 day06 中具体实现

### 4.7 后续优化方向（可选扩展）

- 引入 @Transactional 保证审核操作原子性  ✓
- 增加管理员角色权限校验
- 优化认证记录结构（支持历史记录 or 单记录模式）
- 增加状态流转日志（审计能力）

---
## 五、今日成果总结

- [x] ~~补充 `/api/user/info` 受保护接口，完成“登录成功 → 携带 Token → 查询当前用户信息”链路验证~~
- [x] ~~在 `JwtUtil` 中新增 Token 解析方法 `getUserIdFromToken(...)`~~
- [x] ~~将 `UserController.info()` 从临时写死 `1L` 升级为通过请求头中的 Token 解析真实 `userId`~~
- [x] ~~使用 `UserInfoVO` 对 `/api/user/info` 返回结果进行初步脱敏处理，避免继续直接暴露 `password`~~
- [x] ~~在数据库中完成 `student_cert` 表建表~~
  - 字段包括：`id / user_id / student_name / school / student_id / id_card_front / id_card_back / status / remark / create_time`
- [x] ~~创建学生认证模块基础骨架~~
  - `StudentCert.java`
  - `StudentCertMapper`
  - `StudentCertService`
  - `StudentCertServiceImpl`
  - `StudentCertController`
- [x] ~~实现提交认证申请接口 `/api/cert/submit`~~
  - 当前登录用户通过 Token 解析身份
  - 认证资料成功写入 `student_cert` 表
- [x] ~~实现查询认证状态接口 `/api/cert/status`~~
  - 当前登录用户可以查看自己的认证记录与认证状态
- [x] ~~实现管理员审核接口 `/api/cert/audit`~~
  - 支持根据认证申请记录 `id` 更新审核状态与备注
  - 支持同步更新 `sys_user.auth_status`
- [x] ~~形成 day04 当前阶段的认证主链路闭环~~
  - 提交认证申请 → 查询认证状态 → 管理员审核 → 用户认证状态同步更新
- [x] ~~明确后续商品发布权限校验方向~~
  - 后续将在商品发布接口中基于 `authStatus` 判断用户是否已完成学生认证

> 今日一句话总结：day04 的重点不是单独多做几个接口，而是让“登录态”真正进入业务场景，并进一步把学生认证做成一条可提交、可查询、可审核、可同步用户状态的完整主链路。

---
## 六、下一步任务(day06)

- [x] ~~将 Day 04 认证模块与商品模块打通，形成完整业务闭环~~
  - 在商品发布接口中增加认证状态校验（authStatus == 1）
  - 未认证用户调用发布商品接口 → 返回"请先完成学生身份认证"
  - 验证：带未认证 Token 调用 `/api/product/publish` → 被拦截
- [x] ~~完成商品分类模块基础搭建~~
  - 在 MySQL 中新建 `product_category` 表
  - 创建 ProductCategory 实体类、Mapper、Service、Controller
  - 实现分类列表接口 `/api/category/list`
- [x] ~~完成商品浏览模块~~
  - 在 MySQL 中新建 `product` 表
  - 创建 Product 实体类、Mapper、Service、Controller
  - 实现商品列表接口 `/api/product/list`（只返回上架商品）
  - 实现商品详情接口 `/api/product/detail/{id}`
- [x] ~~完成商品查询模块~~
  - 实现分类筛选（`/api/product/list?categoryId=1`）
  - 实现关键词搜索（`/api/product/list?keyword=xxx`）
  - 实现价格区间筛选（`/api/product/list?minPrice=&maxPrice=`）
- [x] ~~完成商品管理模块~~
  - 实现发布商品接口 `/api/product/publish`（需带 Token）
  - 实现编辑商品接口 `/api/product/update`（只能编辑自己的商品）
  - 实现删除商品接口 `/api/product/delete/{id}`（只能删除自己的商品）
  - 实现我的商品接口 `/api/product/my`
- [x] ~~完成绿叶篇扩展（可选）~~
  - 商品浏览量统计与 Redis 缓存优化
  - 商品搜索体验优化（标题优先于描述）
  - 商品列表分页功能
- [x] ~~将已完成的商品模块接口整理成接口文档，供后续前端或 AI 生成前端时使用~~

---

## 七、踩坑记录

| 问题                                                         | 原因                                            | 解决                                                                |
| ---------------------------------------------------------- | --------------------------------------------- | ----------------------------------------------------------------- |
| `GET /api/user/info` 报错：`Required request body is missing` | 在 GET 接口中误用了 `@RequestBody`，而 GET 请求默认没有请求体   | 去掉 `@RequestBody`，改为无参或使用 `@RequestHeader("token")` 获取用户信息        |
| `/api/user/info` 初期只能固定查 `id = 1`                          | 尚未接入真实登录态，只是为了打通基础链路                          | 先写死 `userId` 测试，后续改为通过 `JwtUtil.getUserIdFromToken(token)` 获取真实用户 |
| 返回用户信息时暴露 `password` 字段                                    | 直接返回数据库实体 `User`，没有做数据隔离                      | 新增 `UserInfoVO`，只返回前端需要字段，避免敏感信息泄露                                |
| 实体字段与数据库字段映射异常                                             | Java 字段与数据库字段命名不规范                            | 统一：数据库下划线（`user_id`），Java 驼峰（`userId`），并确保类型一致                    |
| 查询认证状态时报错：`selectOne found multiple results`               | 同一用户在 `student_cert` 表中存在多条记录                 | 清理测试数据，并在业务层增加“防重复提交”校验                                           |
| 同一账号可以生成多条认证记录                                             | 数据库层没有唯一约束，业务层初期也未限制                          | 在 Service 层增加“先查再插”逻辑（按 `userId + status=0` 校验）                   |
| 已写防重复提交代码，但仍然可以插入多条                                        | 防重复校验条件是 `status = 0`，而旧数据已被审核（状态≠0），不再命中校验条件 | 这是**设计允许的行为**：允许用户在审核结束后重新提交；若要彻底限制，可改为只按 `userId` 判断或加数据库唯一索引    |
| 查询认证状态时误用 `selectById`                                     | 误把业务查询当主键查询                                   | 使用 `LambdaQueryWrapper` 按 `userId` 查询                             |
| 管理员审核后用户状态未同步                                              | 只更新了 `student_cert` 表，没有更新 `sys_user`         | 在 `auditCert()` 中同步更新 `user.authStatus`                           |
| 审核接口未做管理员权限校验                                              | 当前阶段只关注主流程打通，未接入权限系统                          | 后续接入管理员登录态 + 角色权限控制                                               |
| 商品发布权限未落地                                                  | 当前还未开发商品模块                                    | 后续在商品发布接口中校验 `authStatus`，限制未认证用户发布                               |
| 测试过程中数据异常（重复、多条记录）                                         | 使用同一账号反复测试接口，且数据库未清理                          | 定期清理测试数据，或重置测试账号，保证测试结果准确                                         |

> 本阶段问题大多源于“业务约束缺失”与“测试数据干扰”，通过补充 Service 层校验与统一数据规范，认证主链路已趋于稳定。

---
## 八、我话很多：(⁄ ⁄•⁄ω⁄•⁄ ⁄)(2026/04/28)

【作者说：历时三天，终于赶在今天的零点之前写到这里，正式在28号完美收官！(⁄˃ᴗ˂⁄)！昨天跟今天真的很忙，课程排满的同时也意味这我没有自己的时间来学习，我真是把我自己当成一个海棉来挤啊挤(ಠ_ಠ)。现在已经疯狂期待我的五一假期了！我要疯狂赶进度，谁也不能阻止我( ´-ω-)
题外话说完了，开始看看今天的内容吧，说实话我刚开始对这个篇章篇幅很纠结很焦虑，写完这篇文章，字数已经接近23000字，前面的day系列日记都大概在15000字左右，day03是一个例外情况，我试验了一下"龙骨"+"绿叶"的分篇章做笔记，感觉良好，但是这篇写到一半才发现字数严重超标，我在中途也进行了大量的缩减，最后成就现在的字数Σ(っ °Д °;) っ，但我感觉比较满意，因为这篇笔记内容严格按照框架步骤慢慢展开，主要处理了验证阶段的一个接口和学生认证的三个接口，也就是说这篇笔记含金量超高！当然动手量也超大(° ー °〃)至于这篇笔记我为什么没有分开，一是客观原因就是已经很难分开了，这都是具体步骤，根本不好分开；二是主观原因就是我不太想分开，所以字数多是多了点，但内容也都很精！关于今天，实在没有什么精彩的细节值得写在这个日记里，今天一天都是在枯燥乏味的课程上度过，没什么好写的。下面来讲讲规划吧，大纲里的day05是不存在的，因为这部分内容是图片的上传，我暂时没有考虑写它，感觉浪费我开发的时间，所以我准备在最后再写它，明天就直接开始day06的商品体系，首先会跟这篇day04做一个很好的链接，在开始商品体系的征战！๑˃̵ᴗ˂̵)و
    依旧很累啊！！！˚‧º·(˚ ˃̣̣̥᷄⌓˂̣̣̥᷅ )‧º·˚，但是还是要坚持下去的，我向往有追求的生活！
  经过今天的文章篇幅踩大坑，后面的阶段我会认真做好分层事项的，如果可以后续复杂的开发过程我依旧会使用🐉+🌿的组合，这样的逻辑我很喜欢，当然这篇文章的逻辑，我同样喜欢( ´-ω-)
	ok，至此，我在平平无奇的一天完成了day04的笔记，现在准确来说已经是04/29的00：08了，我已经完成了这篇笔记的收官，我会继续把握住这份财富，我会继续采纳向上生长的阳光
收工收工，平淡收工☁️ 🌊 ⛵未完待续，敬请期待day06的内容吧(๑˃̵ᴗ˂̵)و！！！ 】
