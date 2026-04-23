
> 日期：2026/04/19----04/20
> 目标：完成项目骨架搭建，连接数据库，跑通持久层

---

## 项目结构（2026/04/20 更新，04/21更新以day02为准）

> 项目结构已调整为扁平化：**后端独立为一个标准 Spring Boot 项目，直接用 IDEA 打开 `UnSky Market-backend/` 即可开发**；`database` 目录仅存放 SQL 文件，不参与 Maven 构建。

### 当前结构

```
D:\Develop\UnSky Market Project\
│
├── UnSky Market-backend/← ★ （独立 Spring Boot 项目）
│   ├── pom.xml        ← 独立 Maven 项目（继承 Spring Boot Parent）
│   ├── README.md
│   ├── UnSky-Market-项目概述.md
│   ├── Unsky Market 项目日记/        ← 学习笔记
│   └── src/main/
│       ├── java/com/Market/
│       │   ├── UnSkyApplication.java
│       │   └── Common/Entity/User.java
│       └── resources/
│           ├── application.yml
│           └── application-dev.yml
│
└── Unsky Market-database/    ← ★ 仅存放 SQL 文件，不在 Maven 中
    └── day01/
        ├── 01_create_database.sql
        ├── 02_test_data.sql
        ├── 03_query_examples.sql
        └── README.md
```

> **IDEA 打开方式**：直接打开 `D:\Develop\UnSky Market Project\UnSky Market-backend\`，这是标准 Spring Boot 项目，IDEA 会自动识别为 Maven 项目。

---

## 一、数据库层面（Mapper）

### 1.1 创建数据库和表（使用 DataGrip 执行 SQL 脚本）

> **路径变更**（2026/04/20）：SQL 脚本从项目日记移到了 `Unsky Market-database/day01/` 目录下，
> 建议直接在 DataGrip 中打开该目录作为数据库工具窗口，或按顺序执行 `01_create_database.sql` → `02_test_data.sql`。

使用 DataGrip 连接 MySQL，执行以下 SQL（或使用 `Unsky Market-database/day01/01_create_database.sql`）：

使用 DataGrip 连接 MySQL，执行以下 SQL：

```sql
-- 创建数据库
CREATE DATABASE unsky_market DEFAULT CHARSET utf8mb4;

-- 创建用户表
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    phone       VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号（登录账号）',
    password    VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    school      VARCHAR(100) DEFAULT NULL COMMENT '学校',
    student_id  VARCHAR(50)  DEFAULT NULL COMMENT '学号',
    auth_status TINYINT      DEFAULT 0 COMMENT '认证状态（0=未认证 1=已认证 2=审核中）',
    credit_score INT         DEFAULT 100 COMMENT '信用分',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

> **注意事项**
> - `phone` 字段必须 NOT NULL 且 UNIQUE，作为登录账号
> - `password` 字段存加密后的密码，不要存明文（后续会用到 BCrypt）
> - `auth_status` 使用 TINYINT 而非 BOOLEAN，因为有 0/1/2 三种状态
> - `create_time` 用 DATETIME 而非 DATE，保留时分秒

### 1.2 插入测试数据

```sql
INSERT INTO sys_user (nickname, phone, password, school, student_id, auth_status, credit_score)
VALUES ('天下云', '13800138000', '$2a$10$...', 'bilibili大学', '20230001', 1, 100);
```

> `password` 字段的 `$2a$10$...` 是 BCrypt 加密后的占位值，实际密码为 `123456`。后续做完登录功能后再替换真实加密值。

---

## 二、实体层设计（Entity）

### 2.1 包结构一览

```
src/main/java/com/Market/
├── Entity/          ← 实体类，对应数据库表
├── Mapper/          ← Mapper接口，对应数据库操作（下一步）
├── Service/         ← 业务逻辑层（下一步）
├── Controller/     ← 接口层（下一步）
└── Application.java ← 启动类
```

> **注意**：包名统一用大驼峰（PascalCase）`com/Market`，不能写成 `com/market`，否则 IDEA 不会识别为源码根目录。

### 2.2 实体类代码

加入 MyBatis-Plus 注解后，实体类能自动完成很多工作：

> ✅ **已完成**：`UnSky Market-backend/src/main/java/com/Market/Common/Entity/User.java`

```java
package com.Market.Common.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data                  // Lombok：自动生成 getter/setter/toString
@TableName("sys_user") // MyBatis-Plus：映射到数据库表名（请确保表名一致）
public class User {

    @TableId(type = IdType.AUTO) // 主键自增（对应数据库 AUTO_INCREMENT）
    private Long id;

    private String nickname;     // 昵称
    private String phone;         // 手机号（登录账号）
    private String password;      // 密码（登录密码）
    private String avatar;        // 头像路径
    private String school;        // 学校
    private String studentId;     // 学号（MyBatis-Plus 自动映射 snake_case → camelCase）

    // 认证状态（0=未认证 1=已认证 2=认证中）
    private Byte authStatus;

    private Integer creditScore;  // 信用分

    // 注册时间（MyBatis-Plus 自动填充）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

### 2.3 Java 类型与数据库类型对照表

| 数据库类型          | Java 类型        | 说明         |
| -------------- | -------------- | ---------- |
| BIGINT         | Long           | 主键 id      |
| INT / TINYINT  | Integer / Byte | 数字类型，状态，分类 |
| VARCHAR        | String         | 名称、描述、账号   |
| DECIMAL        | BigDecimal     | 价格，金额      |
| DATETIME       | LocalDateTime  | 注册时间       |
| TINYINT（0/1）   | Boolean        | 二元状态       |
| TINYINT（0/1/2） | Byte           | 多元状态       |
| TEXT           | String         | 长文本        |

---

## 三、数据库的连接与验证  (2026/4/22更新内容)

### 3.1 数据库连接配置（application.yml）

在 `src/main/resources/` 下新建 `application.yml`：

```yaml
server:
  port: 8080

spring:
  application:
    name: UnSkyMarket

  # 数据库连接配置（⚠️ 修改 username 和 password 为你的本地值）
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/unsky_market?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: ******

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.Market.Common.Entity
  configuration:
    # 开启 SQL 日志（开发阶段方便调试，上线后关闭）
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    # 核心配置：数据库下划线字段 → Java 驼峰命名自动映射
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto  # 主键自增
```

> **关键配置 `map-underscore-to-camel-case: true`**
> 这行配置使得 `student_id`（数据库）自动映射到 `studentId`（Java），无需手动转换。
> 没有这行配置，`student_id` 永远映射不到 `studentId`，查出来的数据全是 null。

### 3.2 数据访问层准备（Mapper）

1. 创建Mapper接口（如UseMapper）

```java
package com.Market.user.mapper;

import com.Market.common.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper  /*MyBatis 提供的注解，用于标识该接口为 **Mapper 接口（数据访问层）**
          其作用是：
          - 让 MyBatis 在启动时识别该接口
          - 为该接口生成代理实现类
          - 并将其交由 Spring 容器管理（可被 `@Autowired` 注入）*/

public interface UserMapper extends BaseMapper<User> {
}
```

- 通过该接口，可以直接调用 `MyBatis` 提供的方法（如 `selectList`）对数据库进行操作，无需手写SQL

2. 同时，在启动类中通过：
```java
@MapperScan("com.Market")
```    
   扫描 Mapper 接口，使其被 Spring 容器管理。

3. 拓展疑问？？？

 - 为什么 `UserMapper` 可以是空的？
> `UserMapper` 继承了 MyBatis-Plus 提供的 `BaseMapper<User>` 接口。
> `BaseMapper` 已经内置了常用的 CRUD 方法，例如：
> - `insert()`
> - `deleteById()`
> - `updateById()`
> - `selectById()`
> - `selectList()`
>  👉 **所以无需在接口中额外定义方法，也可以直接完成基本的数据库操作**

- 什么时候才需要写方法？
>当出现以下情况时，才需要在 `UserMapper` 中新增方法：
>- 需要自定义查询（复杂条件、多表查询等）
>- 需要手写 SQL（`@Select` / XML）

### 3.3 数据的连接测试

> 在完成数据库连接配置后，需要对连接是否成功进行验证。
> 
> 首先启动Spring Boot项目，通过观察控制台日志判断数据库连接状态。
> 如果项目能够正常启动且没有出现数据库连接异常（如连接失败、认证失败等），
> 则说明数据库连接配置正确。
> 
> 数据库连接成功是后续进行数据操作的前提条件，因此该步骤用于确保系统能够
> 正常访问数据库资源。

### 3.4 数据访问验证(控制台+HTTP)

在这里需要对数据库访问做验证，就用简单的查询来实现这一过程：

1. 数据库表中的数据如下：（通过调用 `MyBatis` 提供的方法（如 `selectList`）进行全表查询）

- 注：此处为便于测试，将密码字段暂时使用明文存储。后续在涉及用户认证功能时，将引入加密算法（如 BCrypt）进行安全处理。

```mysql
INSERT INTO sys_user (nickname, phone, password, avatar, school, student_id, auth_status, credit_score)  
VALUES  
    -- 普通用户  
    ('天下云',    '13800138000', '123456', NULL, 'bilibili大学', '20230001', 1, 100),  
    ('小明同学',  '13800138001', '123456', NULL, '清华大学',     '20230002', 1, 100),  
    ('张三丰',    '13800138002', '123456', NULL, '武当大学',     '20230003', 0, 100),  
    -- 认证中用户  
    ('李四光',    '13800138003', '123456', NULL, '少林大学',     '20230004', 2, 95),  
    -- 信用分异常用户  
    ('王五爷',    '13800138004', '123456', NULL, '华山大学',     '20230005', 1, 70);
```

2. 在项目中编写查询语句来查询结果：(`selectList`--查询整张表)

```java
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
  
    //返回前端（自动转换为 JSON 格式） 
    return users;  
}
```

> 通过在Controller中编写接口方法，调用UserMapper查询数据库中的数据，
并将查询结果输出到控制台，同时返回给前端。
> 该方法实现了数据库与后端之间的数据访问，以及后端向前端的数据传递，
验证了系统各层之间的基本连接关系。

3. 最终结果反馈（包括控制台结果和前端返回结果）：

- 控制台输出结果展示：

```
==>  Preparing: SELECT id,nickname,phone,password,avatar,school,student_id,auth_status,credit_score,create_time FROM sys_user
==> Parameters: 
<==    Columns: id, nickname, phone, password, avatar, school, student_id, auth_status, credit_score, create_time
<==        Row: 1, 天下云, 13800138000, 123456, null, bilibili大学, 20230001, 1, 100, 2026-04-22 10:43:45
<==        Row: 2, 小明同学, 13800138001, 123456, null, 清华大学, 20230002, 1, 100, 2026-04-22 10:43:45
<==        Row: 3, 张三丰, 13800138002, 123456, null, 武当大学, 20230003, 0, 100, 2026-04-22 10:43:45
<==        Row: 4, 李四光, 13800138003, 123456, null, 少林大学, 20230004, 2, 95, 2026-04-22 10:43:45
<==        Row: 5, 王五爷, 13800138004, 123456, null, 华山大学, 20230005, 1, 70, 2026-04-22 10:43:45
<==      Total: 5
```

- 浏览器页面输出结果展示：(`localhost：8081//dbtest`)

```json
[{"id":1,"nickname":"天下
云","phone":"13800138000","password":"123456","avatar":null,"school":"bilibili大学","studentId":"20230001","authStatus":1,"creditScore":100,"createTime":"2026-04-22T10:43:45"},{"id":2,"nickname":"小明同学","phone":"13800138001","password":"123456","avatar":null,"school":"清华大学","studentId":"20230002","authStatus":1,"creditScore":100,"createTime":"2026-04-22T10:43:45"},{"id":3,"nickname":"张三丰","phone":"13800138002","password":"123456","avatar":null,"school":"武当大学","studentId":"20230003","authStatus":0,"creditScore":100,"createTime":"2026-04-22T10:43:45"},{"id":4,"nickname":"李四光","phone":"13800138003","password":"123456","avatar":null,"school":"少林大学","studentId":"20230004","authStatus":2,"creditScore":95,"createTime":"2026-04-22T10:43:45"},{"id":5,"nickname":"王五爷","phone":"13800138004","password":"123456","avatar":null,"school":"华山大学","studentId":"20230005","authStatus":1,"creditScore":70,"createTime":"2026-04-22T10:43:45"}]
```

>- 这里的JSON看起来有点“挤”，主要是因为浏览器默认是一行显示，并不是格式有问题。JSON本身是标准结构数据，只是没有做格式化展示。后续可以通过工具进行美化查看，这类问题在实际开发中很常见，另外JSON是前后端数据交互的标准格式之一( ´◔ ‸◔`)
>
>- 通过以上结果可以看出，数据库查询操作能够正确执行，且数据已成功通过后端接口返回至前端，说明系统已完成数据库 → 后端 → 前端 的完整数据传递验证。

### 3.5 小结----数据库连接

通过以上步骤，完成了数据库连接与数据访问的完整验证：、

```
数据库 → 后端 → 前端
```


系统已具备以下能力：

- 成功连接数据库
- 能够通过 Mapper 查询数据
- 能够在控制台输出结果
- 能够通过接口返回 JSON 数据
- TestController测试成功

👉 实现了数据从数据库到前端的完整传递流程。

后续将在此基础上，进一步实现具体业务功能（如用户登录等）。

---

## 四、启动类（UnskyApplication.java）

项目根目录下新建启动类，这是项目入口，必须有：

```java
package com.Market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication          // 标记为 Spring Boot 启动类
@MapperScan("com.Market") // 扫描 Mapper 接口所在包（注意：这一步 Day 02 才需要）
public class UnskyApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnSkyApplication.class, args);
        log.info("UnSky Market server started: The Little Vessel Sets Sail");
    }
}
```

> **注意**：`@MapperScan` 先不要加，Day 02 创建 `UserMapper` 时再补上，否则会报错。

---

## 五、今日成果总结

- [x] 创建数据库 `unsky_market` 和表 `sys_user`
- [x] 搭建独立 `Spring Boot` 项目骨架（`pom.xml`）
- [x] 配置 JDK 11 编译环境
- [x] 引入 `MyBatis-Plus` + `MySQL` 驱动
- [x] 创建 User 实体类（带注解）
- [x] 完成数据库连接配置（`application.yml`）
- [x] 完成数据库连接与数据访问的完整验证
- [x] 通过简单SQL语句`selectList`全表查询并返回结果
- [x] 分别测试并验证控制台输出和前端页面输出
- [x] 创建启动类 `UnSkyApplication.java`
- [x] 测试启动类`UnSkyApplication.java`正常启动
- [x] 理解 pom 编写规范和 IDEA 配置原理
- [x] 初步将代码上传github仓库，熟悉基本git语法

## 六、下一步任务（day 02）

- 在 Day01 实践过程中，部分原定 Day02 的内容已提前完成,因此此处任务从“基础实现”调整为“结构规范化与工程化完善”，具体任务内容参考大纲day02部分。
  
> 当前已完成：
> 1. 创建 `UserMapper.java` 接口，继承 `BaseMapper<User>`
> 2. 在启动类加上 `@MapperScan("com.Market.mapper")`
> 3. 写一个测试接口，验证能否从数据库查到数据
> 4. 创建数据库连接池配置（可选，Druid）
> 
> day02要完成：
> 1. 完善 Maven 多模块结构，夯实基础（创建父 pom，统一管理 backend 与 common）
> 2. 规范 Common 模块（Result、异常处理、工具类拆分）
> 3. 统一接口返回格式，接入全局异常处理
> 4. 优化项目包结构（统一命名，调整模块层级）
> 5.了解idea运行的完整流程图（理清思路）
> 6.理清Maven模块关系，学习基础概念

- **这部分内容是在 Day02 完成后（2026/4/22）补充回顾整理，相当于一次阶段性总结记录。**
## 七、踩坑记录

| 问题 | 原因 | 解决 |
|------|------|------|
| `student_id` 查出来是 null | 数据库用下划线，Java 用驼峰，未开启映射 | 配置 `map-underscore-to-camel-case: true` |
| MySQL 驱动报错 | 旧版驱动不兼容 JDK 11 | 升级到 mysql-connector-j 8.0.33 |
| IDEA 找不到 Java 类 | 包名大小写问题或不是源码目录 | 检查包结构是否为 `src/main/java/com/Market/` |
| 父 pom 报错"找不到主清单属性" | 父 pom 没有写 `packaging=pom` | 添加 `<packaging>pom</packaging>` |
| IDEA 打开后源码树灰色/不可见 | .iml 文件路径写错或 modules.xml 未引用 | 检查 `.iml` 中 sourceFolder 的 url 路径 |
| 子模块 pom 依赖版本不兼容 | 没写 parent 直接自己指定版本 | 添加 `<parent>` 继承 Spring Boot Parent |

### 补充踩坑记录（2026/04/20 — 目录结构调整后）

| 问题 | 原因 | 解决 |
|------|------|------|
| `application-dev.yml` 扫描不到实体类 | `type-aliases-package` 写成了 `com.Market.Entity` | 改为 `com.Market.Common.Entity` |
| workspace.xml 中路径失效 | 旧路径 `D:/Develop/MyProjects/` 已不存在 | 更新为 `D:/Develop/UnSky Market Project/` |
| IDEA 打开方式错误导致模块找不到 | 打开了错误的目录 | 始终打开 `UnSky Market-backend`（有 pom.xml 的那层） |

### 补充踩坑记录（2026/04/20 — 启动验证阶段）

| 问题 | 原因 | 解决 |
|------|------|------|
| 启动报错 `TypeTag::UNKNOWN` + `ExceptionInInitializerError` | Lombok 版本过旧，不兼容 JDK 11.0.29 的内部编译器 API | 在 `pom.xml` 中显式指定 Lombok 版本 `<version>1.18.30</version>`，覆盖 parent 默认版本 |
| IDEA 中 Lombok 仍然报错 | IDEA 有独立的注解处理器缓存，与 Maven 编译结果不同步 | File → Invalidate Caches → Invalidate and Restart，或删除 `.idea/compiler.xml` + `.idea/workspace.xml` + `target/` 后重新导入项目 |
| `.idea/misc.xml` 中项目 SDK 不匹配 | `project-jdk-name="openjdk-26"` 与实际 JDK 11 不一致，导致 IDEA 内部编译器用错版本 | File → Project Structure → Project → SDK 选 JDK 11；或直接编辑 `misc.xml` 改为 `project-jdk-name="11" languageLevel="JDK_11"` |
| Spring Boot 能启动但 MyBatis 找不到 Mapper | `@MapperScan` 被注释掉，且路径写错（`Unsky-Mapper`） | 取消注释，改为正确路径 `@MapperScan("com.Market.mapper")` |
| `UserMapper.java` 编译通过但运行时报 500 | `UserMapper` 是空接口，没有 `extends BaseMapper<User>`，MyBatis 无法生成代理对象 | 改为 `public interface UserMapper extends BaseMapper<User>` 并添加 `@Mapper` 注解 |
| 端口 8080 被占用，Tomcat 启动失败 | 8080 已被其他进程占用 | 修改 `application.yml` 中的 `server.port` 为其他端口（如 8081），同时在 IDEA 的 Run Configuration 中确认端口一致 |
| 启动日志警告 `Property 'mapperLocations' was not specified` | `mapper-locations` 配置了但没有实际存在的 XML 文件 | 不影响运行（MyBatis-Plus 用注解 SQL），但建议后续添加 Mapper XML 时再填写真实路径 |
| 数据库插入中文测试数据报错 `ERROR 1406 Data too long for column 'nickname'` | SQL 文件编码与 MySQL 数据库 `utf8mb4` 字符集不匹配 | 用 DataGrip 执行 SQL；或命令行执行时加 `--default-character-set=utf8mb4` 参数 |

## 八、我有一点真心话 (･ω･)✧（2026/4/21）

【作者说：到现在为止，项目日记day01的内容也算是圆满收工啦！结果很美好，但是过程很很很...恨狠拫艰辛 (｡•́︿•̀｡) 。上面整个笔记是通过大模型生成的，我做了一小部分的修改。从另一方面讲，这不是笔记，而是一份完整的项目学习实践手册，我跟着上面的的内容，在claude老师的帮助下不断遇山开山，遇水搭桥(〃'▽'〃)，终于到达了day01的目的地。这是我的第一个项目，我想把它做完整，做完美，并在努力中！！！接下来就讲讲我关于这个项目的见解吧！我所做的day01不过是再给一艘超级大船搭建“龙骨”，这是整个船的核心骨架，它要航行必须要在有“龙骨”的前提上，至于它跑的快不快，驶的稳不稳就要看往“龙骨”上加些什么了。这个“龙骨”就连接着船头（前端），也连接着船尾（数据库），现在我的小破船还是差点意思的，我相信它会变成一艘巨轮的(✧ω✧)！接下来就给一些我的小破船历史性时刻留下记忆吧，哈哈哈！！！

(｡･ω･｡)ﾉ♡ 小破船🌊破浪启航✨(测试启动)
![[Pasted image 20260422082633.png|540]]


`ヽ(•̀ω•́ )ゝ` cloud🌊在船头瞭望⛵(测试接口)
![[Pasted image 20260422082507.png|538]]

收工收工，快乐收工！(≧ω≦)/我将会细细咀嚼这篇文章，开启我的首创大船之旅！！！
        未完待续哦！尽情期待(๑˃̵ᴗ˂̵)و！！！ 】