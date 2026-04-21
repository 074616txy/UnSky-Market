# 项目日记 — Day 01：骨架搭建 & 数据库连接

> 日期：2026/04/19----04/20
> 目标：完成项目骨架搭建，连接数据库，跑通持久层

---

## 零、项目结构（2026/04/20 更新）

> 项目结构已调整为扁平化：**后端独立为一个标准 Spring Boot 项目，直接用 IDEA 打开 `UnSky Market-backend/` 即可开发**；`database` 目录仅存放 SQL 文件，不参与 Maven 构建。

### 当前结构

```
D:\Develop\UnSky Market Project\
│
├── UnSky Market-backend/           ← ★ 直接用 IDEA 打开这里（独立 Spring Boot 项目）
│   ├── pom.xml                     ← 独立 Maven 项目（继承 Spring Boot Parent）
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
└── Unsky Market-database/           ← ★ 仅存放 SQL 文件，不在 Maven 中
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

## 三、Maven 依赖配置（pom.xml）

### 3.1 项目 pom.xml（UnSky Market-backend/pom.xml）

项目是独立的 Spring Boot 项目，直接继承 Spring Boot Parent，无多模块嵌套：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- SpringBoot 父工程：2.7.18 是最后一个长期支持 JDK11 的版本 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>com.Market</groupId>
    <artifactId>UnSkyMarket</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <!-- JDK11 编译配置 -->
    <properties>
        <java.version>11</java.version>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Web：后端接口开发必备 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- MyBatis-Plus：提供 BaseMapper 等 CRUD 封装 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.3.1</version>
        </dependency>

        <!-- MySQL 驱动：8.0.33 是 JDK11 下最稳定的版本 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.0.33</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok：简化实体类代码 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Validation：登录参数校验（@Valid / @NotBlank 等） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Boot Test：单元测试支持 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 依赖版本说明

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | JDK 11 最佳匹配，最后一个 2.x LTS |
| MyBatis-Plus | 3.5.3.1 | 功能完整，API 稳定 |
| MySQL Connector | 8.0.33 | 修复了 JDK 11 兼容性问题，推荐 8.0.28+ |
| Lombok | 由 parent 管理 | 无需指定版本 |
| MySQL Server | 8.x | 与 mysql-connector-j 8.x 配套 |

### 3.3 pom.xml 核心要素解析（面试重点）

#### 标准 Spring Boot pom 必须包含的 5 个要素

```xml

<modelVersion>4.0.0</modelVersion>                 <!-- ① 必须有，固定值 -->
<parent>                                             <!-- ② 继承 Spring Boot Parent -->
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-parent</artifactId>
<version>2.7.18</version>
</parent>
<groupId>com.Marketcom.Market</groupId>                        <!-- ③ 公司/组织名，反写域名 -->
<artifactId>UnSkyMarket</artifactId>                <!-- ④ 项目名，唯一标识 -->
<version>1.0-SNAPSHOT</version>                      <!-- ⑤ 版本号，SNAPSHOT=开发版 -->
```

#### `<parent>` 和 `<dependency>` 的区别

| | `<parent>` | `<dependency>` |
|---|---|---|
| 作用 | 继承父 POM 的所有配置 | 把某个 jar 包加入 classpath |
| 解决什么问题 | **谁来管理版本** | **我要用哪些包** |
| 位置 | pom 顶部 | dependencies 节点内 |

> **Spring Boot Parent 解决了什么问题？**
> 如果不用 parent，每个子模块都需要自己声明 spring-boot 所有依赖的版本号，容易出现版本不兼容的问题。Spring Boot Parent 统一管理了所有 Spring 生态的版本，只需要指定用哪些 starter，Maven 自动用经过测试验证的版本组合。

#### `<dependency>` 和 `<dependencyManagement>` 的区别

- **直接写 `<dependency>`**：依赖被立即引入，项目编译时可用
- **`<dependencyManagement>`**（通常只在父 pom 中）：只声明版本，不实际引入；子模块必须显式声明依赖但不写版本，版本从父 pom 继承

> **什么时候用 dependencyManagement？** 当你有多个子模块，且希望统一管理依赖版本时，在父 pom 用 `<dependencyManagement>` 声明所有版本，各子模块继承使用。这样改一个版本号，所有子模块同步生效。

#### Maven 坐标三要素

```
groupId:artifactId:version
com.Market:UnSkyMarket:1.0-SNAPSHOT

Maven 仓库中对应路径：
com/market/UnSkyMarket/1.0-SNAPSHOT/UnSkyMarket-1.0-SNAPSHOT.jar
```

---

## 四、数据库连接配置（application.yml）

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
    password: your_password

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

---

## 五、启动类（Application.java）

项目根目录下新建启动类，这是项目入口，必须有：

```java
package com.Market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication          // 标记为 Spring Boot 启动类
@MapperScan("com.Market.mapper") // 扫描 Mapper 接口所在包（注意：这一步 Day 02 才需要）
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

> **注意**：`@MapperScan` 先不要加，Day 02 创建 `UserMapper` 时再补上，否则会报错。

---

## 六、IDEA 项目结构配置

### 6.1 项目的两层配置

项目同时存在两套配置文件（Maven 的 `pom.xml` 和 IDEA 的 `.idea/`），各自维护配置，互相同步：

| 配置文件 | 归属 | 核心职责 |
|---------|------|---------|
| `.idea/misc.xml` | IDEA | 识别项目类型（JDK 版本、Maven 项目管理器） |
| `.idea/modules.xml` | IDEA | 声明项目中有哪些模块（.iml 文件路径） |
| `.idea/compiler.xml` | IDEA | 编译选项、注解处理器 |
| `.idea/encodings.xml` | IDEA | 文件编码 |
| `.idea/workspace.xml` | IDEA | 项目打开状态、断点、运行配置 |
| `pom.xml` | Maven | 依赖包管理、构建流程、Spring Boot 配置 |

> **项目是扁平的**：后端目录本身就是一个完整的 Spring Boot 项目，不需要嵌套的 .iml 或 modules.xml。IDEA 打开时，pom.xml 会被自动识别为 Maven 项目。

### 6.2 IDEA 打开项目

直接用 IDEA 打开 `D:\Develop\UnSky Market Project\UnSky Market-backend\`，这是标准 Maven 项目，IDEA 会自动：
1. 识别 pom.xml 为 Maven 项目
2. 下载依赖到 External Libraries
3. 在左侧 Project 窗口展示完整项目树
4. 自动配置源码根目录（src/main/java）和资源目录（src/main/resources）

> **无需手动配置**：如果 pom.xml 正确，IDEA 2020+ 会自动处理所有模块配置，不需要手动新建 .iml 或修改 modules.xml。

---

## 七、从打开 IDEA 到运行：完整流程图

```
① 打开项目目录（D:\Develop\UnSky Market Project\UnSky Market-backend）
       │
       ▼
② IDEA 读取 .idea/misc.xml
       ├── 识别为 Maven 项目
       ├── 确定 JDK 版本为 11
       └── 确定项目根目录路径
       │
       ▼
③ IDEA 读取 pom.xml
       └── Spring Boot Parent 自动管理所有依赖版本
       └── Maven 检测到 <parent>spring-boot-starter-parent</parent>
       │
       ▼
④ Maven 下载依赖
       └── pom 声明 spring-boot-starter-web、mybatis-plus...
       └── Maven 从中央仓库下载对应版本的 jar 包
       └── IDEA 右侧 External Libraries 显示已加载的依赖
       │
       ▼
⑤ 左侧 Project 窗口展示项目树
       └── 自动识别 src/main/java 为源码根目录
       └── 自动识别 src/main/resources 为资源目录
       │
       ▼
⑥ 编译
       └── Maven / IDEA 编译 src/main/java 下所有 .java 文件
       └── 语法错误、缺少依赖 在此阶段报错
       │
       ▼
⑦ 运行
       └── 右键 UnSkyApplication.java → Run
       └── Spring Boot 启动
       └── 加载 application.yml
       └── 连接 MySQL
       └── 监听 8080 端口
```

---

## 八、今日成果总结

- [x] 创建数据库 `unsky_market` 和表 `sys_user`
- [x] 搭建独立 Spring Boot 项目骨架（pom.xml）
- [x] 配置 JDK 11 编译环境
- [x] 引入 MyBatis-Plus + MySQL 驱动
- [x] 创建 User 实体类（带注解）
- [x] 完成数据库连接配置（application.yml）
- [x] 创建启动类 UnSkyApplication.java
- [ ] 理解 pom 编写规范和 IDEA 配置原理

## 九、下一步任务（Day 02）

1. 创建 `UserMapper.java` 接口，继承 `BaseMapper<User>`
2. 在启动类加上 `@MapperScan("com.Market.mapper")`
3. 写一个测试接口，验证能否从数据库查到数据
4. 创建数据库连接池配置（可选，Druid）
5. 具体内容参考大纲day02

## 十、踩坑记录

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

## 十一、我有一点真心话 (･ω･)✧

【作者说：到现在为止，项目日记day01的内容也算是圆满收工啦！结果很美好，但是过程很很很...恨狠拫艰辛 (｡•́︿•̀｡) 。上面整个笔记是通过大模型生成的，我做了一小部分的修改。从另一方面讲，这不是笔记，而是一份完整的项目学习实践手册，我跟着上面的的内容，在claude老师的帮助下不断遇山开山，遇水搭桥(〃'▽'〃)，终于到达了day01的目的地。这是我的第一个项目，我想把它做完整，做完美，并在努力中！！！接下来就讲讲我关于这个项目的见解吧！我所做的day01不过是再给一艘超级大船搭建“龙骨”，这是整个船的核心骨架，它要航行必须要在有“龙骨”的前提上，至于它跑的快不快，驶的稳不稳就要看往“龙骨”上加些什么了。这个“龙骨”就连接着船头（前端），也连接着船尾（数据库），现在我的小破船还是差点意思的，我相信它会变成一艘巨轮的(✧ω✧)！接下来就给一些我的小破船历史性时刻留下照片吧，哈哈哈！！！

(｡･ω･｡)ﾉ♡ 小破船🌊破浪启航✨
![[Pasted image 20260420232514.png]]

`ヽ(•̀ω•́ )ゝ` cloud🌊在船头瞭望⛵

![[Pasted image 20260420233112.png|409]]

收工收工，快乐收工！(≧ω≦)/我将会细细咀嚼这篇文章，开启我的首创大船之旅！！！
        未完待续哦！尽情期待(๑˃̵ᴗ˂̵)و！！！ 】