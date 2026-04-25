
> 日期：2026/04/22----04/24
> 目标：成功跑通用户注册登录接口的最后版本，并能在 Apifox 中测试成功

---
> 🎯 本日任务拆解  ---- 🐉+🌿 

> 说明：day03 采用“龙骨 + 绿叶”的记录方式。主篇优先整理用户注册与登录主链路的搭建、联调与跑通；与用户认证和安全相关的增强内容，则单独整理为附属篇 [[项目日记-day03🌿-用户体系 — 用户认证与安全补强]]，避免主篇结构过于冗长。
  
🐉 龙骨（Core）  
- 完成用户注册 & 登录基础链路  
 - [x] 接口打通  
 - [x] 数据库读写正常  
 - [x] Apifox 测试通过  
  
🌿 绿叶（Enhancement）
- 完善关于用户认证与用户安全的相关增强内容
 - [x] 重复注册校验  
 - [x] 密码加密  
 - [x] JWT 认证接入
 - [x] 相关增强内容整理至附属篇 [[项目日记-day03🌿-用户体系 — 用户认证与安全补强]]

---
##  项目结构

- **考虑到项目结构说明中存在较多重复和无关紧要的内容，为避免篇幅冗余并提升笔记可读性，后续仅保留本次修改或新增的结构信息，其余重复部分不再重复展开。**

```
D:\Develop\UnSky Market Project\
└── Unsky-backend/                      ← ★ 主业务模块（Spring Boot）
    ├── pom.xml
    └── src/main/
        ├── java/com.Market
        │   ├── UnSkyApplication        // 启动类
        │   └── user                   // 用户模块
        │       ├── controller
        │       │   ├── TestController
        │       │   └── UserController     // 用户登录、注册接口
        │       ├── mapper
        │       │   └── UserMapper         // 用户数据库操作
        │       ├── service
        │       │   ├── UserService        // 用户业务接口定义
        │       │   └── impl
        │       │       └── UserServiceImpl // 用户业务逻辑实现
        │       └── vo/
        │           └── LoginVO.java     // User+Token返回数据封装
        └── resources
            └── application.yml           // 配置文件
            └── application-dev.yml

```

> 本阶段新增 user 模块，基于分层和分模块架构完成用户登录与注册功能开发，包含 Controller、Service、ServiceImpl、Mapper 四层结构，实现了从接口接入到数据库操作的完整链路。

---

## 一、用户模块结构搭建

> day03 不再只是继续调整项目结构，而是在 day02 已完成工程化与技术基建的基础上，正式落地第一个业务模块 `user`。

### 1.1 user 模块分层结构

本阶段围绕用户体系，新增并明确了 `user` 业务模块的基础分层结构：

- `controller`：负责接收前端请求、调用业务层并返回统一结果
- `service`：负责定义用户注册、登录等核心业务接口
- `service/impl`：负责具体业务逻辑实现
- `mapper`：负责与数据库进行交互，执行用户数据的查询与写入

---

### 1.2 各层职责划分

这次开发继续延续 day02 已经确定下来的分层思路，避免把所有逻辑堆在一个类里：

- `UserController` 负责处理接口入口
- `UserService` 负责定义注册、登录等业务方法
- `UserServiceImpl` 负责承接具体的注册和登录逻辑
- `UserMapper` 基于 MyBatis-Plus 完成用户表的数据访问

> 这样的拆分方式虽然一开始文件会变多，但职责更清晰，也更方便后续继续扩展商品、订单、收藏等模块。

---

### 1.3 开发的整体链路梳理

目前在搭好 `user` 模块基础分层之后，已经把用户功能核心链路基本跑通了：

```text
Apifox → Controller → Service → Mapper → MySQL → 返回结果
```


- 也就是说，请求已经能够正常进入后端，经过业务层处理后完成数据库操作，并把结果返回出来。

- 这一步很重要，因为它意味着项目已经不再只是停留在工程结构搭建阶段，而是正式开始具备真实的业务处理能力。后面的注册完善、登录鉴权、JWT、拦截器等内容，也都会继续沿着这条链路往下扩展。

> `user` 模块的落地，意味着项目已经从“工程骨架搭建阶段”正式进入“业务开发阶段”。后续其他功能模块，也都会参考这一套按业务拆分、按层开发的方式继续推进。

---

## 二、核心代码骨架与顺序（2026/04/24）

### 2.1 UserController

```Java
@RestController //让这个类可以接受HTTP请求的  
@RequestMapping("api/user")//让这个类有统一路径注册→/user/register 登录 → /user/loginpublic class UserController {  
  
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
    /**为什么这里返回类型是Result<User>而不是User？  
     * 因为这里的返回内容是json格式的，里面会包含code,data,msg  
     * 所以这个返回值是跟apifox里面的返回参数对应的  
     * 这里的Result其实就是公共模块common里的类，那里面声明了所有返回类型的组合  
     * Result<User> = 返回一个“包装好的 User 数据”  
     */    public Result<User> login(@RequestBody User user){  
       return userService.login(user);  
    }  
}
```

---

### 2.2 UserService

```Java
/**  
 * 本质上写接口是定义规则  
 * 作用：  
 * 1. 解耦（核心），不用动Controller，它只依赖接口  
 * 2. 可替换 ，可以写出多个接口来随时替换  
 * 3. 规范 ，在团队开发中，接口 = “合同”  
 */public interface UserService {  
  
    /**  
     * 这一层主要是系统必须提供一个注册功能，发送注册请求，将数据存储到数据库  
     * register方法必须与UserController里面的方法对应  
     * @param user  
     * @return  
     */  
    Result<Void> register(User user);  
  
    /**  
     * 这一层主要是系统必须提供一个登录功能，输入 User，返回 Result<User>  
     * login方法必须与UserController里面的方法对应  
     * @param user  
     * @return  
     */  
    Result<User> login(User user);  
}
```

---

### 2.3 UserServiceImpl

```Java
/**  
 * 接口实现类的本质就是实现规则，ServiceImpl = “把请求变成数据库操作，再变成结果返回”  
 * 真正处理业务逻辑  
 * 主要步骤：① 接收参数 ② 构造查询条件 ③ 查数据库 ④ 返回结果  
 * 我要登录网站，把我写的数据传进来，在这里进行操作修改拼装，组成数据库格式，在数据库查找是否有信息，最后返回结果  
 */  
@Service//Spring扫描到这个注解，就会去service包里面找  
public class UserServiceImpl implements UserService {  
  
    @Autowired  
    private UserMapper userMapper;//私有化一个对象userMapper来方便后面使用UserMapper接口  
  
    @Override  
    public Result<Void> register(User user) {  
        /**  
         * 这这里通过userMapper来使用UserMapper接口里的insert方法  
         * 从而实现将注册信息插入数据库的操作  
         */  
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
```

---

### 2.4 UserMapper

```java
@Mapper  
public interface UserMapper extends BaseMapper<User> {}
```

- 后续所有关于操作数据库的接口实现类里面都要通过`@Autowired`  `private UserMapper userMapper`来私有化一个对象`useMapper`来方便后面使用`UserMapper`接口  ，这个接口直接连接提供操作数据库的具体方法

>  确定一下上面文件，由于用的是 **MyBatis-Plus**：
> 👉 所以你**什么都不用写**，直接用现成方法：
> `insert(user)` 👉 插入（注册用）
> `selectOne(wrapper)` 👉 查询（登录用）
> 相当于数据工具人，我可以直接在官方调用操作数据库的方法

---

### 2.5 一个接口从 0 到 1 的基本落地顺序

为了避免一开始写接口时思路混乱，day03 当前更适合先固定一套最基础的开发顺序：

1. `Controller`：先写接口入口，负责接收请求并返回统一结果  
2. `Service`：定义注册、登录等业务能力  
3. `ServiceImpl`：实现具体逻辑，重写对应方法并串起数据库操作  
4. `Mapper`：负责查库、写库，调用 MyBatis-Plus 提供的基础方法

> 这套顺序虽然简单，但对当前阶段非常重要。它能帮助我把“接口入口、业务定义、逻辑实现、数据库操作”四个环节固定下来，避免把代码直接堆在 Controller 里，也避免写着写着就忘了某一层该放什么内容。

---

## 三、用户注册功能实现

### 3.1 注册接口的基本目标

目前用户注册部分的重点，不是一次性把所有细节都补到位，而是先把最基本的注册主链路跑通：

- 能在 Apifox 中发起注册请求
- 后端能正确接收前端传来的注册信息
- 业务层能够处理注册逻辑
- 数据能够成功写入数据库
- 接口能够返回对应结果

---

### 3.2 注册功能的实现流程

当前注册功能的调用链路可以概括为：

```text
Apifox 发起注册请求
→ UserController 接收参数
→ UserService / UserServiceImpl 处理注册逻辑
→ UserMapper 执行插入操作
→ 用户信息写入 MySQL
→ 后端返回注册结果
```

通过这一步，已经验证了用户信息从接口进入后端，再进入数据库的完整链路是通的。

---

### 3.3 注册功能测试结果（2026/04/23）

目前已经完成了注册接口的基础测试：

- 当前统一返回结构重点如下：

| 字段   | 类型      | 必需  | 非空  | 说明      |
| ---- | ------- | --- | --- | ------- |
| code | integer | ✔   | ✔   | 状态码     |
| msg  | string  | ✔   | ✔   | 提示信息    |
| data | null    | ❌   | ❌   | 注册不返回数据 |

- 可以在 Apifox 中发送注册请求

![[Pasted image 20260424163805.png]]

```json
{
    "code": 0,
    "msg": "操作成功",
    "data": null
}
```
- 后端能够正确接收到参数

![[Pasted image 20260424145247.png]]

- 数据库中能够看到新插入的用户数据

![[Pasted image 20260423215547.png]]

这说明“前端请求 → 后端处理 → 数据库存储”这一条主链路已经具备基本可用性。

---

### 3.4 注册功能的后续完善方向

虽然当前注册主流程已经跑通，但后续仍然需要继续补充和优化：

- 增加重复注册校验，避免手机号或账号重复
  相关补充详见：[[项目日记/项目日记-day03🌿-用户体系 — 用户认证与安全补强#二、重复注册校验机制补充|重复注册校验机制补充]]
- 对密码进行加密存储，提升安全性
  相关补充详见：[[项目日记/项目日记-day03🌿-用户体系 — 用户认证与安全补强#三、密码加密逻辑完善|密码加密逻辑完善]]
- 补充参数合法性校验
- 细化异常提示与返回信息

---

## 四、用户登录功能实现

### 4.1 登录接口的基本目标

目前登录部分的核心目标，是先验证账号登录这条业务链路是否能够跑通，而不是一步到位完成完整鉴权体系。

当前重点已经放在：

- 能在 Apifox 中发起登录请求
- 后端能够接收登录参数
- 能根据登录信息前往数据库查询用户数据
- 能完成基础比对并返回登录结果

---

### 4.2 登录功能的实现流程

当前登录链路可以整理为：

```text
Apifox 发起登录请求
→ UserController 接收登录信息
→ UserService / UserServiceImpl 处理登录逻辑
→ UserMapper 查询数据库中的用户信息
→ 后端进行账号信息比对
→ 返回登录成功或失败结果
```

这一步的意义在于，项目已经不再只是“能查表”，而是开始具备真正的业务判断能力。

---

### 4.3 登录功能测试结果

登录功能目前也完成了基础测试验证：

- 当前登录相关字段重点如下：

|字段名|类型|必需|非空|中文名|说明|
|---|---|---|---|---|---|
|id|integer|✔|✔|用户ID|数据库主键|
|username|string|✔|✔|用户名|登录账号|
|password|string|✔|✔|密码|当前是明文（后续会加密）|

- 可以在 Apifox 中发送登录请求
1. 登录成功截图与json返回数据展示：

![[Pasted image 20260423215330.png]]

```json
{
    "code": 0,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "nickname": "天下云",
        "phone": "13800138000",
        "password": "123456",
        "avatar": null,
        "school": "bilibili大学",
        "studentId": "20230001",
        "authStatus": 1,
        "creditScore": 100,
        "createTime": "2026-04-22T10:43:45"
    }
}
```

2. 登录失败截图和json返回数据展示：

> 注：登录失败有多种情况，这里只展示一种----账号和密码都不对的情况，
> 其他大致返回信息都大差不差，就不在这里过多赘述！˃ ֫ ֫ ˂

![[Pasted image 20260424145834.png]]

```json
{
    "code": 500,
    "msg": "用户名或密码错误",
    "data": null
}
```
- 后端能够接收到前端传来的登录信息

![[Pasted image 20260424150131.png]]

- 可以根据这些信息到数据库中进行查询和比对

![[Pasted image 20260423215547.png]]

- 最终能够返回对应的登录结果

```Java
/**  
 * 统一返回格式：所有 Controller 接口统一返回此类  
 * @param <T> data 字段的类型  
 */  
@Data //相当于getter/setter/toString  
@NoArgsConstructor//无参构造  
@AllArgsConstructor//全参构造  
public class Result<T> {  
  
    private Integer code;   // 状态码：0=成功，其他=失败  
    private String msg;     // 信息描述  
    private T data;        // 泛型数据体  
    /**  
     * 成功返回，无数据  
     */  
    public static <T> Result<T> success() {  
        return new Result<>(0, "操作成功", null);  
    }  
  
    /**  
     * 成功返回，带数据  
     */  
    public static <T> Result<T> success(T data) {  
        return new Result<>(0, "操作成功", data);  
    }  
  
    /**  
     * 成功返回，自定义消息  
     */  
    public static <T> Result<T> success(String msg, T data) {  
        return new Result<>(0, msg, data);  
    }  
  
    /**  
     * 失败返回，自定义错误码和消息  
     */  
    public static <T> Result<T> error(Integer code, String msg) {  
        return new Result<>(code, msg, null);  
    }  
  
    /**  
     * 失败返回，默认错误码 500  
     */    public static <T> Result<T> error(String msg) {  
        return new Result<>(500, msg, null);  
    }  
}
```

这说明用户模块已经完成了“注册 + 登录”两条核心业务链路的基础跑通。

---

### 4.4 登录功能的后续完善方向

登录功能当前仍属于基础版本，后续还需要继续完善：

- 登录成功后返回 JWT Token
  相关补充详见：[[项目日记/项目日记-day03🌿-用户体系 — 用户认证与安全补强#三、密码加密逻辑完善|密码加密逻辑完善]]
- 使用加密后的密码进行比对 
  相关补充详见：[[项目日记/项目日记-day03🌿-用户体系 — 用户认证与安全补强#四、JWT 认证链路补强|JWT 认证链路补强]]
- 细化登录失败场景的返回提示
- 为后续的接口鉴权、拦截器校验做好准备
  当前已完成登录成功后的 Token 生成与返回，后续仍可继续围绕 Token 校验、接口放行和受保护接口验证展开补充

---

## 五、今日成果总结

- [x] 确定后续开发继续沿用 `com/Market/` 下按业务分模块扩展的方式
- [x] 在 `user` 模块下明确 `controller / mapper / service / service/impl` 四层职责
- [x] 搭出 `UserController`、`UserMapper`、`UserService`、`UserServiceImpl` 的基础骨架以及代码内
- [x] 基本跑了一遍整体代码链路并进行了简单的接口测试
- [x] 实现注册接口 `/api/user/register`
  - 参数：手机号 + 密码
  - 要点：参数接收、基础校验、调用 Service、写入数据库
- [x] 完善登录接口 `/api/user/login`
  - 完成账号密码校验
  - 接受JWT令牌完成身份校验
- [x] 统一让用户模块接口返回 `Result.success(...) / Result.error(...)`
- [x] 让 day02 中已经完成的全局异常处理继续服务 day03，避免注册/登录报错时返回格式混乱
- [x] 将 `JwtUtil` 正式用于登录成功后的 Token 颁发
- [x] 新增登录拦截器与配置类，放行注册/登录接口，保护需要登录才能访问的接口
- [x] 使用 Apifox 按顺序完成测试：注册成功 → 登录成功 → 拿 Token → 访问受保护接口

> 补充说明：考虑到重复注册校验、密码加密、JWT 认证等内容虽然仍属于 day03 的学习范围，但在结构上更适合作为“主链路跑通后的增强专题”单独展开，因此另附记录于 [[项目日记-day03🌿-用户体系 — 用户认证与安全补强]]。这样既保留了主篇的主线清晰度，也完整保留了绿叶部分的细化过程。

> 今日一句话总结：day03 的重点不是把用户系统一次性全部做完，而是先把“项目能启动、接口能请求、数据能入库、Apifox 能测通”这条主链路真正接起来。只有这根主骨头先立住，后面的登录完善、JWT、拦截器和鉴权能力才有继续往下扩展的基础。

---

## 六、下一步任务(day04)

- [ ] 补一个 `/api/user/info` 之类的受保护接口，验证“登录 → 带 Token 访问”这条链路是否打通

---

## 七、踩坑记录

| 问题                                                | 原因                                 | 解决                                                     |
| ------------------------------------------------- | ---------------------------------- | ------------------------------------------------------ |
| 注册成功但数据库没有数据                                      | 实际没有执行 `insert`，只是返回了成功结果          | 在 ServiceImpl 中确认 `userMapper.insert(user)` 被执行，并加日志排查 |
| Apifox 返回成功但逻辑没执行                                 | 请求路径错误或没打到正确接口                     | 确认 URL 与 `@RequestMapping` 一致（如 `/user/register`）      |
| 使用 GET 请求调用接口报错                                   | 接口定义为 `@PostMapping`               | 在 Apifox 中改为 POST 请求                                   |
| login 一直返回 ok                                     | Controller 还是测试壳子，没有写真实逻辑          | 替换为数据库查询逻辑（`selectOne`）                                |
| ServiceImpl 写了但不生效                                | 没加 `@Service` 注解，Spring 没扫描到       | 在实现类上加 `@Service`                                      |
| 调用 Service 报 MyBatis 错误                           | Service 被误当成 Mapper 扫描             | 不要让 `@MapperScan` 扫到 service 包                         |
| 报错：Invalid bound statement (UserService.register) | `@MapperScan("com.Market")` 扫描范围过大 | 改为：`@MapperScan("com.Market.*.mapper")`                |
| Mapper 扫描扩展问题                                     | 只扫描单模块，后续模块失效                      | 使用通配：`com.Market.*.mapper`                             |
| data 返回结构写错                                       | 把 data 写成 string，而不是对象/null        | 登录：data=User；注册：data=null                              |
| 注册接口不写请求参数                                        | 误以为注册不需要输入                         | 注册必须传 username/phone + password                        |
| 返回密码给前端                                           | 不理解数据返回边界                          | 注册不返回数据，登录后也不建议返回密码                                    |
| Service 接口设计混乱                                    | 重复定义 login 方法、返回 String            | 统一为 `Result<User> login(User user)`                    |
| Controller 直接写数据库逻辑                               | 没分层，结构混乱                           | Controller → Service → Mapper 分层                       |
| ServiceImpl 代码写在类外                                | 没写在方法里，导致接口未实现                     | 所有逻辑必须写在 `@Override` 方法内                               |
| Service 只定义了方法但没有真正落地逻辑                           | 只写了接口能力，没有在实现类中完成对应业务                | 注册、登录等方法必须在 `UserServiceImpl` 中用 `@Override` 正式实现      |
| user 未定义报错                                        | 方法没有参数 user                        | 方法必须定义 `login(User user)`                              |
| userMapper 为 null                                 | 没有注入 Mapper                        | 添加 `@Autowired private UserMapper userMapper;`         |
| 忘记重启项目                                            | Spring 缓存旧代码                       | 修改后必须重启                                                |
| 导入无关包导致干扰                                         | 引入错误 static import                 | 删除无关 import                                            |
| Apifox 不知道怎么测接口                                   | 不熟悉工具使用                            | POST + JSON + 正确 URL                                   |
| 想把项目直接导入 Apifox                                   | 误解工具工作方式                           | 用 Swagger/OpenAPI 或手动调试                                |

---

## 八、我继续说：٩(๑>◡<๑)۶!!!(2026/04/25)

【作者说：这几天是周末，原本想的是痛痛快快的写三天代码和笔记，但是天不遂人愿(ಥ_ಥ)。周五打了会球然后兄弟过生日(生日宴在昨天，但是好像过生日是在前天，也就是xxxx/04/23)，我们出去吃大餐，超级开心！🎉٩(˃̶͈̀௰˂̶͈́)و，在这里也写一下哈（椰子味Happy every day🎉！！！）今天也是超级忙超级累，我也是从十二点多左右写到当前五点，我也是仅仅完成了一部分内容，晚上又约球了(≧∀≦) ゞ，我得努努力，赶快完成day03的所有内容(包括绿叶部分🌿)，绿叶部分还差最后的操作逻辑顺序和代码展现以及测试部分没有呈现，然后就在今天下午就可以在codex老师的帮助下完成这一篇章，原本打算的是两天完成一篇dayxx，现在看来结构复杂的确实挺难实现，总之继续写吧，这篇day03涉及初步开发接口的设定，所以要精细一些，认真一点！(๑•̀ㅂ•́)و✧
我相信熟悉之后，后面写这种接口速度肯定不在话下，gogogo！！！
    暂时写这一部分吧，剩下的话等到晚上再由心生吧(•ө•)♡！！！----17：12
现在是22：00就在刚刚大概花了有1个小时左右的时间完成了绿叶篇章的🌿所有内容，美美的收尾啦！！！！┏ (^ω^)=☞时隔三天，俺终于做到了！但是明天依旧要练车，仍然是累泪嘞啊(ಥ_ಥ)，实话实说，练车也是挺好玩的ฅ՞•ﻌ•՞ฅ今天晚上怎么说呢，codex老师差点意思，慢了好多，那也没招，但是好在最终还是卡点完成任务！！！
--   ʚïɞ 在这讲讲我的学习方法和思路，关于这一篇文章还是比较特别的，它是一个"龙
骨"+"绿叶"🐉+🌿 的组合，也就是"核心主篇"+"补强附篇"的组合，就像一种壮志豪情的文曲搭配精美旋律的词调，最终目的就是尽快感受到做项目的成就感，然后顺着这种成就感继续开发，会有源源不断的力气！也是能让这篇文章层次逻辑结构上可以更清晰更清楚，也是方便后面的复习！有主有次，方是全章！(づ๑・̀д・́๑) づ
     OK，至此day03的所有内容就此完结，这是一篇充满激情的文章，这篇文章集结
了我所有的想法与所踩的坑，以及在这个过程中领悟到的学习方法和思想，我相信这是一个宝贵的财富，学习永无止境，计算机就是无底洞，学不完根本学不完(ﾟДﾟ≡ﾟдﾟ)!?
收工收工，满足收工☁️ 🌊 ⛵未完待续，敬请期待day04的内容吧(๑˃̵ᴗ˂̵)و！！！ 】
