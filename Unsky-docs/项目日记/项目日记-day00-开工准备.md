# 项目日记 — Day 00：开工准备

> 日期：2026/04/18
> 目标：工具安装完毕，能打开 IDEA，看到完整的空项目树

---

## 一、工具安装清单

### 1.1 IDEA 安装与配置

- 下载安装 IntelliJ IDEA（推荐 2024.1 以上版本）
- 配置 JDK 11：
  - `File` → `Project Structure` → `SDKs` → 添加本地 JDK 11 安装路径
  - `Project` → `SDK` 选择 11，语言等级选择 11
- 安装常用插件：
  - `Maven Helper`（pom 依赖可视化）
  - `Lombok`（可选，IDEA 2020+ 内置支持）
  - `MyBatisX`（Mapper 接口与 XML 跳转）

### 1.2 MySQL 安装

- 下载 MySQL 8.x（推荐 8.0.45 或更高）
- 安装时选择 `Custom` 安装，配置端口 3306
- 设置 root 密码（记住，后续配置要用）
- 安装完成后验证：`mysql -u root -p` 能登录即成功

### 1.3 DataGrip 安装与连接

- 下载 DataGrip（JetBrains 全家桶之一）
- 新建连接：
  - `Host`: localhost
  - `Port`: 3306
  - `User`: root
  - `Password`: 你设置的密码
- 测试连接成功，即可用 SQL 操作数据库

### 1.4 Git 安装

- 下载 Git for Windows
- 安装完成后，在项目根目录右键 → `Git Bash Here`
- 初始化仓库：`git init`
- 配置用户名邮箱（后续提交用）：
  ```bash
  git config --global user.name "YourName"
  git config --global user.email "your@email.com"
  ```

---

## 二、认识项目目录结构

项目分为两个独立的目录：

```
D:\Develop\UnSky Market Project\
│
├── UnSky Market-backend/           ← ★ 直接用 IDEA 打开这里（后端代码）
│   ├── pom.xml                     ← 独立 Spring Boot Maven 项目
│   └── src/main/
│       ├── java/com/Market/
│       │   └── UnSkyApplication.java   ← 启动类（Day 01 创建）
│       └── resources/
│           └── application.yml         ← 配置文件（Day 01 创建）
│
└── Unsky Market-database/              ← 仅存放 SQL 文件（不在 Maven 中）
    └── day01/
        └── *.sql                    ← 建表脚本、测试数据
```

> **为什么目录名有空格（UnSky Market）？**
> 这是项目名本身包含的空格，IDEA 和 Maven 都能正常处理。如果你是新创建项目，建议把项目名改为不带空格的格式（如 `UnSkyMarket`），可以避免后续配置文件中的路径歧义问题。

---

## 三、在 IDEA 中打开项目

1. `File` → `Open` → 选择 `D:\Develop\UnSky Market Project\UnSky Market-backend\` 文件夹
2. IDEA 识别为 Maven 项目后，右侧 `Maven` 工具窗口会显示项目树
3. 点击 `Reload All Maven Projects`（刷新图标），让 Maven 下载依赖
4. 验证左侧 `Project` 窗口能看到完整的项目结构
5. 如果源码目录（src）显示为灰色，右键 → `Mark Directory as` → `Sources Root`

---

## 四、今日踩坑记录

| 问题 | 原因 | 解决 |
|------|------|------|
| IDEA 打开后源码显示灰色 | 没有标记为源码根目录 | 右键 src → `Mark Directory as` → `Sources Root` |
| IDEA 打开后看不到 Maven 项目 | 打开了错误的目录 | 确保打开的是 `UnSky Market-backend`（有 pom.xml 的那层） |
| Maven 依赖下载失败 | 网络问题或镜像源未配置 | 在 `settings.xml` 中配置阿里云镜像 |
| MySQL 连接不上 | 服务未启动 | Win+R → `services.msc` → 启动 MySQL 服务 |

---

## 五、下一步（Day 01）

完成骨架跑通：创建 pom.xml、application.yml、实体类、启动类，跑通 Java → MySQL 链路。
