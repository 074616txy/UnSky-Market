

> 日期：2026/05/09----05/10
> 目标：管理员能管理用户、商品、认证审核

---

## 一、管理员模块基础结构搭建

### 1.1 管理员表设计

**在数据库中创建`admin`表(使用 DataGrip 执行 SQL 脚本)**：

```mysql
-- ============================================  
-- UnSky Market - Day10 管理员表建表脚本  
-- 数据库：unsky_market  
-- 对应后端实体：admin  
-- 字符集：utf8mb4（支持 emoji 和特殊字符）  
-- ============================================  
  
CREATE TABLE admin (  
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',  
    username VARCHAR(50) NOT NULL COMMENT '管理员账号',  
    password VARCHAR(100) NOT NULL COMMENT '管理员密码',  
    role VARCHAR(50) DEFAULT 'ADMIN' COMMENT '管理员角色',  
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
    UNIQUE KEY uk_username (username)  
) COMMENT = '管理员表';
```

### 1.2 Admin 实体类设计

```Java
@Data  
@TableName("admin")  
public class Admin {   
    // 管理员ID  
    private Long id;  
    // 管理员账号  
    private String username;  
    // 管理员密码  
    private String password;  
    // 管理员角色  
    private String role;  
    // 创建时间  
    private LocalDateTime createTime;  
}
```

### 1.3 模块基础结构搭建

1. 创建 `AdminMapper`数据访问层

```Java
@Mapper  
public interface AdminMapper extends BaseMapper<Admin> {}
```

2. 创建 `AdminService`接口方法声明

```Java
public interface AdminService {}
```

3. 创建 `AdminServiceImpl`接口实现类

```Java
@Service  
@RequiredArgsConstructor  
public class AdminServiceImpl implements AdminService {}
```

4. 创建 `AdminController`接口控制层

```Java
@RestController  
@RequestMapping("/api/admin")  
@RequiredArgsConstructor  
public class AdminController {  
    private final AdminService adminService;  
}
```

- 当前结构展示：

```text
admin
├── controller
│   └── AdminController
├── entity
│   └── Admin
├── mapper
│   └── AdminMapper
└── service
    ├── AdminService
    └── impl
        └── AdminServiceImpl
```

### 1.4 AdminLoginDTO 与 AdminVO 设计

1. 创建前端返回结构 `AdminLoginDTO`

```JAVA 
@Data  
public class AdminLoginDTO {  
    // 管理员账号  
    private String username;   
    // 管理员密码  
    private String password;  
}
```

2. 创建返回给前端结构`AdminVO`

```Java
@Data  
public class AdminVO {  
    // 管理员ID  
    private Long id;  
    // 管理员账号  
    private String username;  
    // 管理员角色  
    private String role;  
    // 登录 token
    private String token;  
}
```

---

## 二、管理员登录接口实现

### 2.1 管理员账号初始化

**在数据库中执行`admin`初始化脚本(使用 DataGrip 执行 SQL 脚本)**：

```mysql
-- ============================================  
-- UnSky Market - Day10 管理员账号初始化数据  
-- 表：admin  
-- 说明：用于插入一个管理员账号  
-- ============================================  
  
INSERT INTO admin (username, password, role)  
VALUES ('Skyron', 'BCrypt加密后的密码', 'ADMIN');
```

> 在这里我是**直接在现有 Spring Boot 项目里临时写一个测试类生成加密密码**，然后将加密密码插入管理员初始化脚本中，这样数据库存储的`BCrypt` 校验后的密码，但是管理员账号的密码依旧是123456 (⊙ˍ⊙)

### 2.2 管理员登录参数校验

1. 在 `AdminService` 添加管理员登录的方法

```Java
/**  
 * 管理员登录  
 * @param adminLoginDTO  
 * @return  
 */  
Result<AdminVO> login(AdminLoginDTO adminLoginDTO);
```

2. 在 `AdminServiceImpl`里面实现管理员登录的基础校验

```Java
/**  
 * 管理员登录  
 * @param adminLoginDTO  
 * @return  
 */  
@Override  
public Result<AdminVO> login(AdminLoginDTO adminLoginDTO) {  
    // 1. 判断参数是否为空  
    if (adminLoginDTO == null) {  
        return Result.error("登录参数不能为空");  
    }  
    // 2. 判断账号是否为空  
    if (!StringUtils.hasText(adminLoginDTO.getUsername())) {  
        return Result.error("管理员账号不能为空");  
    }  
    // 3. 判断密码是否为空  
    if (!StringUtils.hasText(adminLoginDTO.getPassword())) {  
        return Result.error("管理员密码不能为空");  
    }  
    // 4. 根据账号查询管理员  
    LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Admin::getUsername, adminLoginDTO.getUsername());  
    
    Admin admin = adminMapper.selectOne(wrapper);  
    if (admin == null) {  
        return Result.error("管理员账号不存在");  
    }  
    return Result.success();  
}
```

### 2.3 密码校验与 Token 生成

1. 在 `AdminServiceImpl` 里注入 `BCryptPasswordEncoder`

```Java
private final BCryptPasswordEncoder passwordEncoder;
```

2. 在管理员登录实现方法`login`后面添加密码校验

```Java
/**  
 * 管理员登录  
 * @param adminLoginDTO  
 * @return  
 */  
@Override  
public Result<AdminVO> login(AdminLoginDTO adminLoginDTO) {  
    // 1. 判断参数是否为空  
    if (adminLoginDTO == null) {  
        return Result.error("登录参数不能为空");  
    }  
    // 2. 判断账号是否为空  
    if (!StringUtils.hasText(adminLoginDTO.getUsername())) {  
        return Result.error("管理员账号不能为空");  
    }  
    // 3. 判断密码是否为空  
    if (!StringUtils.hasText(adminLoginDTO.getPassword())) {  
        return Result.error("管理员密码不能为空");  
    }  
    // 4. 根据账号查询管理员  
    LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Admin::getUsername, adminLoginDTO.getUsername());  
    
    Admin admin = adminMapper.selectOne(wrapper);  
    if (admin == null) {  
        return Result.error("管理员账号不存在");  
    }  
    // 5. 校验密码-->这里要创建一个密码加密工具 passwordEncoder，对密码进行加密和校验
    if (!passwordEncoder.matches(adminLoginDTO.getPassword(), admin.getPassword())) {  
        return Result.error("管理员密码错误");  
    }  
    return Result.success();  
}
```

3. 在 `JwtUtil` 里实现管理员登录生成token

⚠️ 这里为了区分普通用户 token 和管理员 token，所以在 `JwtUtil` 里新增一个方法

```Java
/**  
 * 生成管理员Token  
 * @param adminId  
 * @return  
 */  
public static String generateAdminToken(Long adminId) {
    Date now = new Date();
    Date expireDate = new Date(now.getTime() + EXPIRE_TIME);
    
    return Jwts.builder()
            .setSubject(String.valueOf(adminId))
            .claim("role", "ADMIN")
            .setIssuedAt(now)
            .setExpiration(expireDate)
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
            .compact();
}
```

> ⚠️ 这里需要注意要让管理员 `token` 和普通用户 `token` 使用同一套签名方式

4. 在`login`方法后面补充生成管理员`token`

```Java
// 6. 生成管理员token  
String token = JwtUtil.generateAdminToken(admin.getId());
```

5. 在`login`方法后面补充封装 `AdminVO`

```Java
// 7. 封装返回结果  
AdminVO adminVO = new AdminVO();  
adminVO.setId(admin.getId());  
adminVO.setUsername(admin.getUsername());  
adminVO.setRole(admin.getRole());  
adminVO.setToken(token);

return Result.success(adminVO);
```

### 2.4 管理员登录接口实现

- 在 `AdminController` 添加登录接口

```Java
/**  
 * 管理员登录  
 * @param adminLoginDTO  
 * @return  
 */  
@PostMapping("/login")  
public Result<AdminVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {  
    return adminService.login(adminLoginDTO);  
}
```

### 2.5 接口测试与验证

- 在`Apifox`里测试并验证管理员登录

![[Pasted image 20260510153801.png]]

> 成功展示正确的返回结果，管理员登录接口测试成功，管理员登录链路跑通。

---

## 三、管理员用户管理功能实现

### 3.1 用户列表查询接口

🎯 目标：**管理员查看所有普通用户列表**

1. 在 `AdminService` 添加查询用户列表的方法

```Java
/**  
 * 查询用户列表  
 * @return  
 */  
Result<List<User>> listUsers();
```

2. 在 `AdminServiceImpl` 实现查询用户列表的具体方法

```Java
/**  
 * 查询用户列表  
 * @return  
 */  
@Override  
public Result<List<User>> listUsers() {  
    // 查询所有普通用户  
    List<User> userList = userMapper.selectList(null);  
    return Result.success(userList);  
}
```

3. 在 `AdminController` 添加查询用户列表的接口

```Java
/**  
 * 查询用户列表  
 * @return  
 */  
@GetMapping("/users")  
public Result<List<User>> listUsers() {  
    return adminService.listUsers();  
}
```

4. 在`Apifox`里面进行测试与验证

![[Pasted image 20260510155024.png]]

> 当前先跑通管理员用户列表，下一步再处理返回结构返回加密密码的安全问题。

### 3.2 封禁用户接口实现

1. 在 `AdminService` 添加封禁用户的方法

```Java
/**  
 * 封禁用户  
 * @param userId  
 * @return  
 */  
Result<Void> banUser(Long userId);
```

2. 在 `AdminServiceImpl` 实现封禁用户的具体方法

```Java
/**  
 * 封禁用户  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> banUser(Long userId) {  
    // 1. 判断用户ID是否为空  
    if (userId == null) {  
        return Result.error("用户ID不能为空");  
    }    
    // 2. 查询用户是否存在  
    User user = userMapper.selectById(userId);  
    if (user == null) {  
        return Result.error("用户不存在");  
    }  
    // 3. 判断是否已经被封禁  
    if (user.getStatus() != null && user.getStatus() == 0) {  
        return Result.error("用户已被封禁");  
    }  
    // 4. 修改状态为封禁  
    user.setStatus((byte) 0);  
    userMapper.updateById(user);  
    
    return Result.success();  
}
```

3. 在 `AdminController` 添加封禁用户的接口

```Java
/**  
 * 封禁用户  
 * @param userId  
 * @return  
 */  
@PutMapping("/users/ban/{userId}")  
public Result<Void> banUser(@PathVariable Long userId) {  
    return adminService.banUser(userId);  
}
```

4. 在`Apifox`里面进行测试与验证

![[Pasted image 20260510162811.png]]

> 这里封禁`userId`=5，"王五爷"的帐号，数据库中的`status`字段对应的值变为0

### 3.3 解封用户接口实现

1. 在 `AdminService` 添加解封用户的方法

```Java
/**  
 * 解封用户  
 * @param userId  
 * @return  
 */  
Result<Void> unbanUser(Long userId);
```

2. 在 `AdminServiceImpl` 实现解封用户的具体方法

```Java
/**  
 * 解封用户  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> unbanUser(Long userId) {  
    // 1. 判断用户ID是否为空  
    if (userId == null) {  
        return Result.error("用户ID不能为空");  
    }    
    // 2. 查询用户是否存在  
    User user = userMapper.selectById(userId);  
    if (user == null) {  
        return Result.error("用户不存在");  
    }  
    // 3. 判断是否已经是正常状态  
    if (user.getStatus() != null && user.getStatus() == 1) {  
        return Result.error("用户已经是正常状态");  
    }  
    // 4. 修改状态为正常  
    user.setStatus((byte) 1);  
    userMapper.updateById(user);  
    
    return Result.success();  
}
```

3. 在 `AdminController` 添加解封用户的接口

```Java
/**  
 * 解封用户  
 * @param userId  
 * @return  
 */  
@PutMapping("/users/unban/{userId}")  
public Result<Void> unbanUser(@PathVariable Long userId) {  
    return adminService.unbanUser(userId);  
}
```

4. 在`Apifox`里面进行测试与验证

![[Pasted image 20260510163553.png]]

> 这里解封`userId`=5，"王五爷"的帐号，数据库中的`status`字段对应的值变为1

---

## 四、管理员商品与认证管理实现（⭐）

### 4.1 商品列表管理接口

🎯 目标：**管理员查看所有商品列表**

1. 在 `AdminService` 添加管理员查询商品列表的方法

```Java
/**  
 * 管理员查询商品列表  
 * @return  
 */  
Result<List<Product>> listProducts();
```

2. 在 `AdminServiceImpl` 实现管理员查询商品列表的具体方法

```Java
/**  
 * 管理员查询商品列表  
 * @return  
 */  
@Override  
public Result<List<Product>> listProducts() {  
    // 管理员查询所有商品，不限制状态  
    List<Product> productList = productMapper.selectList(null);  
    return Result.success(productList);  
}
```

3. 在 `AdminController` 添加管理员查询商品列表的接口

```java
/**  
 * 管理员查询商品列表  
 * @return  
 */  
@GetMapping("/products")  
public Result<List<Product>> listProducts() {  
    return adminService.listProducts();  
}
```

4. 在`Apifox`里面进行测试与验证

![[Pasted image 20260510164220.png]]

> 🚀 管理员查询商品列表可以查询所有状态的商品，包括正常上架商品、下架商品、违规商品、已售商品，方便管理员进行更好的审核和风控

### 4.2 下架违规商品接口

🎯 目标：**管理员把某个商品下架**

1. 在 `AdminService` 添加下架商品的方法

```Java
/**  
 * 下架商品  
 * @param productId  
 * @return  
 */  
Result<Void> offShelfProduct(Long productId);
```

2. 在 `AdminServiceImpl` 实现下架商品的具体方法

```Java
/**  
 * 下架商品  
 * @param productId  
 * @return  
 */  
@Override  
public Result<Void> offShelfProduct(Long productId) {  
    // 1. 判断商品ID是否为空  
    if (productId == null) {  
        return Result.error("商品ID不能为空");  
    }  
    // 2. 查询商品是否存在  
    Product product = productMapper.selectById(productId);  
    if (product == null) {  
        return Result.error("商品不存在");  
    }  
    // 3. 判断商品是否已经下架  
    if (product.getStatus() != null && product.getStatus() == 0) {  
        return Result.error("商品已经下架");  
    }  
    // 4. 修改商品状态为下架  
    product.setStatus((byte) 0);  
    productMapper.updateById(product);  
    
    return Result.success();  
}
```

3. 在 `AdminController` 添加下架商品的接口

```Java
/**  
 * 下架商品  
 * @param productId  
 * @return  
 */  
@PutMapping("/products/off/{productId}")  
public Result<Void> offShelfProduct(@PathVariable Long productId) {  
    return adminService.offShelfProduct(productId);  
}
```

4. 在`Apifox`里面进行测试与验证

![[Pasted image 20260510164917.png]]

> 👉 这里下架`productId`=14的商品，数据库中该商品的状态值变为0

### 4.3 认证申请列表查询

🎯 目标：**管理员查看所有学生认证申请**

1. 在 `AdminService` 添加的查询认证申请列表的方法

```Java
/**  
 * 查询认证申请列表  
 * @return  
 */  
Result<List<StudentCert>> listStudentCerts();
```

2. 在 `AdminServiceImpl` 实现查询认证申请列表的具体方法

```Java
/**  
 * 查询认证申请列表  
 * @return  
 */  
@Override  
public Result<List<StudentCert>> listStudentCerts() {  
    // 查询所有认证申请  
    List<StudentCert> certList = studentCertMapper.selectList(null);  
    return Result.success(certList);  
}
```

3.  在 `AdminController` 添加查询认证申请列表的接口

```Java
/**  
 * 查询学生认证申请列表  
 * @return  
 */  
@GetMapping("/certifications")  
public Result<List<StudentCert>> listStudentCerts() {  
    return adminService.listStudentCerts();  
}
```

4. 在`Apifox`里面进行测试与验证

![[Pasted image 20260510165650.png]]

> 管理员接口是查看所有人的认证申请，这里成功展示出所有学生认证申请列表，接口测试成功

### 4.4 学生认证审核接口

🎯 目标：**管理员审核学生认证，通过或拒绝**

1. 创建审核`CertAuditDTO`

```Java
@Data  
public class CertAuditDTO {  
    // 认证ID  
    private Long certId;  
    // 审核状态：1通过，2拒绝  
    private Byte status;  
}
```

2. 在 `AdminService` 添加审核认证的方法

```Java
/**  
 * 审核学生认证  
 * @param certAuditDTO  
 * @return  
 */  
Result<Void> auditStudentCert(CertAuditDTO certAuditDTO);
```

3. 在 `AdminServiceImpl` 实现审核学生认证的具体方法

```Java
/**  
 * 审核学生认证  
 * @param certAuditDTO  
 * @return  
 */  
@Override  
public Result<Void> auditStudentCert(CertAuditDTO certAuditDTO) {  
    // 1. 判断参数是否为空  
    if (certAuditDTO == null || certAuditDTO.getCertId() == null) {  
        return Result.error("认证ID不能为空");  
    }   
    // 2. 判断审核状态是否合法  
    if (certAuditDTO.getStatus() == null  
            || (certAuditDTO.getStatus() != 1 && certAuditDTO.getStatus() != 2)) {  
        return Result.error("审核状态只能是1通过或2拒绝");  
    }  
    // 3. 查询认证申请是否存在  
    StudentCert cert = studentCertMapper.selectById(certAuditDTO.getCertId());  
    if (cert == null) {  
        return Result.error("认证申请不存在");  
    }  
    // 4. 判断是否已经审核过  
    if (cert.getStatus() != null && cert.getStatus() != 0) {  
        return Result.error("该认证申请已审核");  
    }  
    // 5. 修改认证状态  
    cert.setStatus(certAuditDTO.getStatus());  
    studentCertMapper.updateById(cert);  
    
    return Result.success();  
}
```

4. 在 `AdminController` 添加审核学生认证的接口

```Java
/**  
 * 审核学生认证  
 * @param certAuditDTO  
 * @return  
 */  
@PutMapping("/certifications/audit")  
public Result<Void> auditStudentCert(@RequestBody CertAuditDTO certAuditDTO) {  
    return adminService.auditStudentCert(certAuditDTO);  
}
```

5. 在`Apifox`里面进行测试与验证

![[Pasted image 20260510171050.png]]

> 这里返回结果展示学生认证审核已通过，数据库的相应字段状态值变为1

### 4.5 当前阶段边界

📌 当前 Day10 的后台管理模块先做基础版本，重点是让管理员具备最核心的平台管理能力。

✔ 当前已经完成的内容包括：

- 管理员账号表设计；
- 管理员登录；
- 管理员 Token 生成；
- 查询普通用户列表；
- 封禁用户；
- 解封用户；
- 查询所有商品列表；
- 下架违规商品；
- 查询学生认证申请列表；
- 审核学生认证申请。

📚 当前后台管理模块暂时不处理的内容包括：

- 多级管理员权限；
- 超级管理员与普通管理员区分；
- 管理员操作日志；
- 后台数据统计面板；
- GMV 统计；
- 今日新增订单统计；
- 用户增长趋势；
- 商品审核流；
- 复杂 RBAC 权限模型。

> ✨ 这些内容都属于后台管理系统的后续扩展。当前阶段先保证管理员能够完成最基础的平台管理操作，包括用户管理、商品管理和认证审核。

---

## 五、今日成果总结

- [x] 完成管理员表 `admin` 设计
- [x] 完成管理员模块基础结构搭建
  - `Admin` 实体类
  - `AdminMapper` 数据访问层
  - `AdminService` 业务接口
  - `AdminServiceImpl` 业务实现类
  - `AdminController` 接口控制层
- [x] 完成 `AdminLoginDTO` 登录参数设计
- [x] 完成 `AdminVO` 登录返回结构设计
- [x] 初始化管理员账号
  - 使用 BCrypt 生成加密密码
  - 在数据库中插入管理员账号
- [x] 实现管理员登录接口 `/api/admin/login`
  - 完成账号参数校验
  - 完成 BCrypt 密码校验
  - 生成管理员 Token
  - 返回管理员基础信息和 Token
- [x] 实现管理员用户管理功能
  - 查询普通用户列表
  - 封禁用户
  - 解封用户
- [x] 补充用户状态字段 `status`
  - `1`：正常
  - `0`：封禁
- [x] 实现管理员商品管理功能
  - 查询所有商品列表
  - 下架违规商品
- [x] 实现学生认证管理功能
  - 查询所有学生认证申请
  - 审核学生认证申请
- [x] 梳理后台管理模块当前阶段边界
  - 当前先完成管理员登录、用户管理、商品管理、认证审核
  - 多级权限、操作日志、数据统计、复杂 RBAC 后续再扩展

---
## ## 六、下一步任务(day11)

- [x] 确认项目最终部署目标
  - 将 UnSky Market 从本地开发环境部署到公网环境
  - 最终通过公网 IP 访问项目页面
  - 完成项目从“本地可运行”到“公网可展示”的收尾
- [x] 完成本地前端部署形态验证
  - 前端开发环境使用 Vite 运行在 `127.0.0.1:5173`
  - 执行 `npm run build` 生成前端 `dist`
  - 使用本地 Nginx 容器托管 `dist`
  - 理解 Vite 开发服务器和 Nginx 静态资源部署的区别
- [x] 准备阿里云 ECS 云服务器
  - 创建 Ubuntu 22.04 云服务器
  - 获取公网 IP
  - 配置 root 密码登录
  - 使用 SSH 从本地连接服务器
- [x] 在服务器安装 Docker 环境
  - 安装 `docker.io`
  - 启动 Docker 服务
  - 安装 Docker Compose
  - 配置 Docker 镜像加速
  - 使用 `hello-world` 验证 Docker 可用
- [x] 上传项目部署文件到服务器
  - 上传前端 `dist`
  - 上传后端 Spring Boot `jar`
  - 上传数据库初始化 SQL
  - 统一放到 `/opt/unsky-market` 部署目录
- [x] 使用 Docker 启动 MySQL 容器
  - 创建 `unsky_market` 数据库
  - 挂载数据卷保存数据库数据
  - 导入项目初始化 SQL
  - 处理中文字符集问题
- [x] 使用 Docker 启动 Redis 容器
  - 启动 Redis 7 容器
  - 使用 `redis-cli ping` 验证 Redis 正常运行
- [x] 使用 Docker 启动 Spring Boot 后端容器
  - 挂载后端 `backend.jar`
  - 通过环境变量配置 MySQL 和 Redis 连接
  - 启动后端服务并监听 `8081` 端口
  - 使用接口测试确认后端连接数据库成功
- [x] 使用 Docker 启动 Nginx 前端容器
  - 使用 Nginx 托管前端 `dist`
  - 配置前端路由回退到 `index.html`
  - 配置 `/api` 反向代理到 Spring Boot 后端
  - 统一通过 80 端口对外提供访问
- [x] 配置阿里云安全组
  - 开放 80 端口
  - 允许公网浏览器访问项目页面
  - 解决服务器内部正常但公网无法访问的问题
- [x] 完成部署问题排查
  - 排查 502 问题
  - 区分服务器内部访问和公网访问
  - 解决接口数据中文乱码问题
  - 重新初始化 MySQL 数据并指定 `utf8mb4` 字符集
- [x] 完成最终公网访问验证
  - 前端页面可以通过公网 IP 访问
  - `/api` 请求可以由 Nginx 正确转发到后端
  - 后端可以正常连接 MySQL 和 Redis
  - 项目完成从本地开发到公网部署的完整闭环

---

## 七、踩坑记录

| 问题 | 原因 | 解决 |
| --- | --- | --- |
| 管理员密码不能直接存明文 | 登录校验使用 BCrypt，数据库明文密码无法通过加密匹配 | 临时生成 BCrypt 加密密码，再插入 `admin` 表 |
| 管理员 Token 生成时报 `Illegal base64 character: '-'` | 管理员 Token 的签名方式和普通用户 Token 不一致，错误把密钥当 Base64 解析 | 改成和普通用户一致的签名方式：`Keys.hmacShaKeyFor(SECRET_KEY.getBytes())` |
| 登录成功后不能返回 `password` | 密码属于敏感信息，即使是加密后的也不应该返回前端 | 使用 `AdminVO` 返回 `id`、`username`、`role`、`token` |
| 管理员和普通用户不能混用一张表 | 普通用户和管理员身份、权限、业务场景不同 | 单独创建 `admin` 表和管理员模块 |
| 用户管理不能直接删除用户 | 用户可能关联商品、订单、评价、认证等历史数据 | 使用 `status` 字段实现封禁和解封 |
| 商品违规处理不能直接删除商品 | 商品可能关联订单、收藏、购物车、评价等数据 | 管理员只修改商品 `status`，将商品下架 |
| `Byte` 状态字段不能随便用 `equals(1)` | `1` 默认是 `Integer`，容易出现类型比较失败 | 使用 `status != 1` 或将 DTO 状态字段也设计为 `Byte` |
| 学生认证审核状态类型不一致 | `StudentCert.status` 是 `Byte`，如果 DTO 用 `Integer` 容易混乱 | `CertAuditDTO.status` 改为 `Byte`，保持类型一致 |
| 管理员接口当前还没有权限拦截 | 当前只实现了管理员功能接口，尚未做 admin token 拦截校验 | 后续可增加管理员拦截器，限制普通用户访问 `/api/admin/**` |
| 用户列表直接返回实体类可能暴露敏感字段 | `User` 实体中可能包含密码等不该返回的数据 | 当前先跑通功能，后续可改为 `UserVO` 返回 |

---

## 八、我依旧想说：ᕦ(ò_óˇ)ᕤ（2026/05/10）

【作者说：时隔两天我也是迅速的把day10的内容学完了，这一篇文章主要涉及后台管理体系，将登录用户分为普通用户和管理员，这两者的边界必须要分清楚(๑•̀ㅂ•́)و 当然由于前面`user`实体类设计的不是很完全，也重新回到day01进行了补充和添加，至此也是成功的解决了所有的问题，但是这个项目的确不像那些大的电商平台一样包含面很广泛，机制很完善，我这只能做一个练手项目来让我们更清楚的感受到项目的各个模块和业务，让我们能够切身经历对每一个接口的真实测试以及更加熟练的是使用`Apifox`，这都是我们宝贵的财富，所以我认为`UnSky Market`是一个很适合我们的项目！✧(｡•̀ᴗ-)✧⭐ ✨
    今天早上依旧出去练车，但是这一次来练车练的比较少，时隔多天没有练习导致我练的有些糟糕，确实很痛苦啊(ಥ_ಥ) ，但是后来又在他人帮助下和自己的不断摸索下也是成功掌握了，我明白一个道理，练得越多越熟，练得越投入越爽！(≧▽≦) 练的次数少，但我练的很精💡！这也可以延伸到学习中去，不需要天天学习，疯狂叠加学习次数，就算你把图书馆的门槛踏破，也无法真实的拉高你的上限，只有在每一次的学习中有所得有所想，学得精，这样才会提高你的上限，当然在这种高精力学习的情况下踏破图书馆门槛，那这个世界上对你而言就没有困难的事情了(⊙_⊙?) 🔥高精力学习是很重要的，就像我这个笔记就不是很符合，它简直又臭又长，但是这是第一个项目我写的就会细节一些，流程就会更加严格；到后面的学习我仍然会以高精力学习为目标🎯，越写越少，越写越精 (๑˃̵ᴗ˂̵)و 
在完成这个笔记的同时，我也在和`codex`一起研究怎么样写出一个符合我项目的好看的...(这里省略一万个正向形容词)的前端界面，但是可能是我讨论不佳，感觉做出来的不是很符合我的心意，我后面会继续钻研学习，后面会更好的展示这个项目，当然关于这个系统的学习，华友最后一个day系列的内容----day11 关于项目的落地，我会把这个项目布置到公网上去，随后再创建一个我的个人ip网站，在上面存这些我自己制作的项目以及一些小玩意！(｡･ω･｡)🚀我很期待啊！(｡♥‿♥｡)
  今天确实也没有啥特别的内容了，我还得完善一下大纲的内容，原本今天就打算部署项目并且做一个完美的前端，但是我还是想的简单了，并且我的脚好了，所以又去打了很久的球🏀所以确实没能在今天完成所有内容，但是我能保证明天一定完成！✧(｡•̀ᴗ-)✧
至此，这个项目的核心操作以及重要的内容已经全部完成，只剩下最后的收官🚀，我很期待，在这些学习过程中我懂得了太多太多，我的目标也越来越坚定，我在发现我不断朝着我想象的样子前进，**心之所向，素履可往**！收工收工，期待完工！☁️ 🌊 ⛵未完待续，敬请期待day11的内容吧(๑˃̵ᴗ˂̵)و！！！ 】
