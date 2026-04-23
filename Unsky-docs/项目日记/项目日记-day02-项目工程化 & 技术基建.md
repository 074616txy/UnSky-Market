
> 日期：2026/04/21----04/22
> 目标： Maven 多模块结构完成，统一返回格式和全局异常处理上线

---

## 项目结构

```
D:\Develop\UnSky Market Project\
│
├── pom.xml                    ← ★ 父工程（聚合 Backend + Common）
├── .gitignore
│
├── Unsky-backend/                  ← ★ 主业务模块（Spring Boot）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/Market/
│       │   ├── UnSkyApplication.java
│       │   ├── controller/ ← ★主模块的控制层（对外入口）  
│       │   ├── service/ ← ★主模块的服务层（业务逻辑）  
│       │   └── mapper/ ← ★主模块的数据访问层（数据库操作）
│       │
│       └── resources/
│           ├── application.yml
│           └── application-dev.yml
│
├── Unsky-common/                  ← ★ 公共模块（被 Backend 依赖）
│   ├── pom.xml
│   └── src/main/
│       └── java/com/Market/common/
│           ├── Entity/User.java
│           ├── exception/
│           └── Result.java
│
├── Unsky-database/               ← ★ SQL 脚本目录（不参与 Maven）
│
└── Unsky-docs/                     ← ★ 文档与项目日记
    ├── README.md
    ├── UnSky-Market-项目大纲.md
    └── 项目日记/
```

* 这是经过整理的全新模块，day02后面的框架都以这个为参考标准。

---

## 一、Maven 基础速查（从单模块到多模块）

> 本节汇总 day01 中涉及的核心 Maven 概念，作为进入多模块工程化前的知识铺垫。

### 1.1 单模块 pom.xml 长什么样

day01 的独立 Spring Boot 项目 pom.xml（无多模块）：

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

### 1.2 依赖版本说明

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | JDK 11 最佳匹配，最后一个 2.x LTS |
| MyBatis-Plus | 3.5.3.1 | 功能完整，API 稳定 |
| MySQL Connector | 8.0.33 | 修复了 JDK 11 兼容性问题，推荐 8.0.28+ |
| Lombok | 由 parent 管理 | 无需指定版本 |
| MySQL Server | 8.x | 与 mysql-connector-j 8.x 配套 |

### 1.3 pom.xml 核心要素解析

#### 标准 Spring Boot pom 必须包含的 5 个要素

```xml
<modelVersion>4.0.0</modelVersion>                 <!-- ① 必须有，固定值 -->
<parent>                                             <!-- ② 继承 Spring Boot Parent -->
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>
<groupId>com.Market</groupId>                        <!-- ③ 公司/组织名，反写域名 -->
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

### 1.4 IDEA 与 Maven 的双层配置

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

### 1.5 从打开 IDEA 到运行：完整流程图

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

## 二、理清模块框架

### 2.1 Maven 模块关系
D:\Develop\UnSky Market Project\
│
├── pom.xml                      ← ★ 项目总配置文件（管理所有模块依赖）
│  
├── Unsky-backend/          ← ★ 后端业务模块
│   │
│   ├── pom.xml                ← ★ 依赖 Unsky-common
│   │  
│   ├── user/                       ← 用户模块
│   │  
│   ├── ...                             ← 后续扩展模块   
│   │
│   └── other/                      ← 其他业务模块
├── Unsky-common/          ← ★ 公共基础模块（工具类、异常处理、统一返回）
│   └── pom.xml
│
├── Unsky-database/           ← ★ 数据库脚本目录（独立存放 SQL）
│   
└── Unsky-docs/                  ← ★ 项目文档与开发日记

### 2.2 Maven 模块重要代码

#### ① 根目录 `pom.xml`
```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.market</groupId>
    <artifactId>unsky-market-project</artifactId>
    <version>1.0-SNAPSHOT</version>

    <packaging>pom</packaging>

    <modules>//这里是子模块
        <module>Unsky-backend</module>//子模块Unsky-backend
        <module>Unsky-common</module>//子模块Unsky-common
        <module>Unsky-...</module>//子模块...扩展
    </modules>

</project>
```
> 作用：管理整个项目，注册所有模块，做模块聚合     

### ② Unsky-backend/pom.xml
```xml
<project>

    <parent>
        <groupId>com.market</groupId>
        <artifactId>unsky-market-project</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>Unsky-backend</artifactId>

    <dependencies>

        <!-- 依赖公共模块 -->
        <dependency>
            <groupId>com.market</groupId>
            <artifactId>Unsky-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

    </dependencies>

</project>
```
> 重点：在该模块Unsky-backend的pom.xml下要写清楚它通过dependency来声明依赖公共模块Unsky-common，Unsky-backend就可以调用公共模块中的任何包。 在依赖生效后，backend 才能 import common 中的类 (●'◡'●)

### ③ Unsky-common/pom.xml
```xml
<project>

    <parent>
        <groupId>com.market</groupId>
        <artifactId>unsky-market-project</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>Unsky-common</artifactId>

</project>
```

## 三、Maven核心概念与注意事项

### 3.1 Maven、pom.xml 与依赖的概念和关系


| 核心名词 |    Maven    |   pom.xml    |         依赖         |
| :--: | :---------: | :----------: | :----------------: |
| 本质概念 | 项目构建与依赖管理工具 | Maven 的配置文件  |   对外声明，对其他模块产生依赖   |
| 形象类比 |    项目总管     |  总管手里的施工图纸   |     部门之间的协作申请单     |
|  作用  |    执行管理     |     定义规则     |       建立调用关系       |
| 负责内容 | 构建、打包、模块管理  |  项目配置、模块声明   |      引入外部模块或库      |
| 相互关系 | 读取 pom 执行项目 |  被 Maven 读取  | 写在 pom 中由 Maven 解析 |
| 缺失后果 |   项目无法构建    | Maven 无规则可执行 |     模块无法调用所需能力     |
|  区别  |     谁来管     |    按什么规则管    |      管哪些协作关系       |

### 3.2 Maven注意事项

>  1. `artifactId` 不允许有空格，建议全小写，单词用 - 连接
>  2. `groupId`、`artifactId`、`version` 在 parent 中必须与父 pom 一致。
>  3. `<module>` 写的是模块目录名，不是 Java 包名。
>  4. `backend` 引入 `common` 必须通过 `<dependency>` 配置。
>  5.  根 pom 必须设置：  `<packaging>pom</packaging>`
>  6. 被依赖模块必须有自己的 pom.xml，否则 Maven 不识别。
>  7. 模块依赖靠 Maven dependency，不是靠 Spring 注解

## 四、关键原则

1. 谁启动，谁依赖 Common
    - backend 是启动模块，在 pom.xml 中声明对 common 的依赖
    - common 不能依赖 backend，否则循环依赖
  2. 版本号统一在父 POM 管理
    - 父 POM 的 `<properties>` 定义版本号变量
    - 父 POM 的 `<dependencyManagement>` 锁定版本
    - 子模块引入时只写 `groupId` + `artifactId`，不写 version
  3. Spring Boot 插件只在启动模块配置
    - `spring-boot-maven-plugin` 只在 `Unsky-backend` 配置
    - `Unsky-common` 不需要打包成可执行 JAR
  4. 模块编译顺序由 Maven 自动推导
    - Maven 会根据依赖关系自动决定编译顺序
    - 被依赖的模块（如 `Unsky-common`）会先编译

## 五、今日成果总结

- [x] 深度理解项目由Maven单模块向多模块的转变
- [x] 在根目录`pom.xml`中声明根目录的新增模块，深度理解模块聚合
- [x] 配置正确的依赖版本，整理依赖版本说明
- [x] 理清楚Maven、pom.xml 与依赖的概念和关系，并进行整理
- [ ] 学习了解`<parent>` ，`<dependency>`和 `<dependencyManagement>`  的区别和用法
- [ ] 整理根目录 `pom.xml`，`Unsky-backend/pom.xml`，`Unsky-common/pom.xml`的相关重要代码
- [ ] 清楚关于xml格式的注意事项，避免踩坑
- [ ] 了解关于Maven的关键原则，版本统一、依赖关系等
- [ ]  更新迭代项目目录生成day02的框架，并在大纲中修改
- [ ]  完善更新项目日记-day01中的数据库链接与验证，基本实现数据库 → 后端 → 前端
- [ ]  重新梳理项目日记与项目大纲的内容，对框架有了更深的了解

## 六、下一步任务(day03)


### day03 建议推进顺序（结合残篇内容补全）

- [x] 确定后续开发继续沿用 `com/Market/` 下按业务分模块扩展的方式
- [x] 在 `user` 模块下明确 `controller / mapper / service / service/impl` 四层职责
- [x] 搭出 `UserController`、`UserMapper`、`UserService`、`UserServiceImpl` 的基础骨架
- [ ] 实现注册接口 `/api/user/register`
  - 参数：手机号/用户名 + 密码
  - 要点：参数接收、基础校验、调用 Service、写入数据库
- [ ] 完善登录接口 `/api/user/login`
  - 先完成账号密码校验
  - 再接入 JWT Token 返回
- [ ] 统一让用户模块接口返回 `Result.success(...) / Result.error(...)`
- [ ] 让 day02 中已经完成的全局异常处理继续服务 day03，避免注册/登录报错时返回格式混乱
- [ ] 将 `JwtUtil` 正式用于登录成功后的 Token 颁发
- [ ] 新增登录拦截器与配置类，放行注册/登录接口，保护需要登录才能访问的接口
- [ ] 补一个 `/api/user/info` 之类的受保护接口，验证“登录 → 带 Token 访问”这条链路是否打通
- [ ] 使用 Apifox 按顺序完成测试：注册成功 → 登录成功 → 拿 Token → 访问受保护接口

### 这一阶段最重要的理解

- day01 解决的是“**有没有链路**”
- day02 解决的是“**链路是否规范、是否方便继续扩展**”
- day03 解决的是“**在规范链路上，能不能真正做出第一个业务模块**”

> 也就是说，day02 的“下一步任务”本质上不是再补理论，而是把 day02 搭好的骨架，交给 day03 去承载真正的业务开发。

## 七、踩坑记录

| 问题                             | 原因                                    | 解决                                             |
| ------------------------------ | ------------------------------------- | ---------------------------------------------- |
| 数据库表数据“消失”                     | 实际不是丢失，而是数据库未重新创建或连接错库                | 重新执行 SQL 脚本创建表，并确认连接的数据库名称正确                   |
| 是否需要同时创建 `user` 和 `sys_user` 表 | 对业务模型理解不清，不确定表职责划分                    | 明确：普通用户与系统用户分离设计时才需要两个表，否则只保留一个                |
| Mapper 接口作用不清晰                 | 对 MyBatis-Plus 的 `BaseMapper` 继承机制不了解 | `Mapper` 只需继承 `BaseMapper<T>` 即可自动拥有 CRUD 方法   |
| `@Mapper` 注解不理解                | 不清楚 Spring 如何扫描 Mapper 接口             | `@Mapper` 用于标识该接口为 MyBatis 映射接口，交由 Spring 容器管理 |
| Mapper 注释不会写                   | 不知道如何描述接口职责                           | 注释重点写清：功能（数据访问）、作用（操作哪张表）、依赖（MyBatis-Plus）     |
| 数据库设计犹豫（是否拆表）                  | 缺乏系统设计经验，容易过度设计                       | 当前阶段优先简单设计，后续有权限/角色再拆分表结构                      |

## 八，我想说：٩(˃̶͈̀௰˂̶͈́)!!! (2026/4/22)

【作者说：今天课程确实有点多，但我感觉我还是完成了好多好多内容，除了在day02的模块理论外，还有day01里面的数据库，后端与前端的连接。也就是运用了一下简单的SQL语句，这些语句是`MyBatis-Plus`官方自带的全表查询，也是成功在前端显示了这个表的全部数据（包括字段和存储数据），做到这里其实还是有一点小兴奋的✧( •̀ω•́ )✧，今天这篇笔记我是自己手搓的(๑•̀ㅂ•́)و✧，说实话确实有点难总结，我要是token再多一点嘞！！！٩(ˊᗜˋ*)و，当然这些笔记我肯定会来来回回啃很多遍的，毕竟这是我的第一个项目！！！完成这篇笔记的所有内容（其实不是很准确，因为日日看日日新，我后面肯定会部分继续修改迭代的）我又简单开启了一下day03的内容，现在这艘船的“龙骨”已经是基本搭建好了，day03主要是解决船门🚪和船票的事了，说实话我很期待嘿嘿！！┏ (^ω^)=☞    skyron也是费劲千辛万苦完成出了第一个接口----TestController，这个接口也是成功开启测试，这不过这测试没有用第三方接口软件Apifox，主要是在网页端进行的，后面的接口都会在这个app上进行，这样可能方便点，当然这也是一个程序员必备的技能！！！(˶˃ ᵕ ˂˵)
zzz…(～﹃～)~zZ  累累累 ！！！
   另外我也修改项目大纲，再分模块方面我把框架做了最终的定型，我觉得这很重要，所以最终结果也是我深思熟虑的结果！٩(˃̶͈̀௰˂̶͈́)و最后，都说做成一件事都是从0到1最难，我也是深深的体会到了，希望后面的内容善待我吧  (｡•́︿•̀｡)，☁️ 🌊 ⛵收工收工，疲惫手工(˃̵ᴗ˂̵)و】



