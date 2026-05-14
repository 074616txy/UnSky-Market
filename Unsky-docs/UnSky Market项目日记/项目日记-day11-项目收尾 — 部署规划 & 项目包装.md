

> 日期：2026/05/11----05/12
> 目标：：把 UnSky Market 从本地开发环境部署到公网环境，让别人可以通过公网 IP 访问项目页面。

---

## 一、部署目标

这次部署的目标不是单纯把后端跑起来，而是把整个 UnSky Market 项目真正放到公网环境中。

最终要达到的效果是：

```text
浏览器访问公网 IP
  ↓
看到前端页面
  ↓
前端通过 /api 请求后端
  ↓
后端连接 MySQL 和 Redis
  ↓
页面正常展示商品、订单、评价等数据
````

最终公网访问地址：

```text
http://118.31.40.151
```

这次部署使用 Docker 来运行各个服务：

```text
MySQL 容器
Redis 容器
Spring Boot 后端容器
Nginx 前端容器
```

---

## 二、本地部署形态确认

一开始我的前端是在本地开发环境中运行的：

```text
http://127.0.0.1:5173
```

这个地址对应的是 **Vite 开发服务器**。

也就是说，`5173` 不是正式部署环境，而是前端开发时用来热更新、调试页面的服务。

---

### 2.1 前端打包

前端页面开发完成后，需要先执行打包命令：

```bash
npm run build
```

打包后会生成：

```text
dist
```

这个 `dist` 才是真正部署时要交给 Nginx 托管的静态文件。

---

### 2.2 本地 Nginx 容器演练

为了提前理解部署形态，我先在本地用 Docker 跑了一个 Nginx 容器，把前端 `dist` 挂载进去：

```bash
docker run -d --name vue-nginx -p 8082:80 -v D:/Develop/UnSky-Market/dist:/usr/share/nginx/html nginx
```

本地访问：

```text
http://localhost:8082
```

这里的含义是：

```text
8082 = 我电脑上的访问端口
80 = Nginx 容器内部端口
```

所以可以理解为：

```text
5173 是 Vite 开发服务器
8082 是 Nginx 托管 dist，更接近真实部署环境
```

这一步让我先明白：  
**开发时访问 5173，部署时访问 Nginx。**

---

## 三、准备云服务器

这次我使用的是阿里云 ECS 云服务器。

配置如下：

```text
系统：Ubuntu 22.04 64位
规格：2核 2GiB
系统盘：40GiB
公网 IP：118.31.40.151
```

我理解的云服务器就是：

```text
一台有公网 IP 的 Linux 电脑
```

本地项目只能我自己访问，但云服务器有公网 IP，所以别人可以通过浏览器访问它。

---

### 3.1 试用规则理解

阿里云这里有 300 元免费抵扣额度，但是它本质上还是按量计费。

需要注意：

```text
试用结束不会自动释放
不用时要手动停止或释放实例
```

这一点很重要，不然服务器一直开着可能会继续扣费。

---

### 3.2 SSH 登录问题

创建服务器后，我设置了 root 密码，但是发现 SSH 默认不允许密码登录。

所以我先通过阿里云网页远程连接进入服务器，然后修改 SSH 配置。

先备份配置文件：

```bash
cp /etc/ssh/sshd_config /etc/ssh/sshd_config.bak
```

然后开启 root 登录和密码登录：

```bash
sed -i 's/^#\?PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config
sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config
```

重启 SSH 服务：

```bash
systemctl restart ssh
```

然后在 Windows PowerShell 里连接服务器：

```bash
ssh root@118.31.40.151
```

这一步的作用是：  
**让我可以直接从本地终端登录云服务器，后面上传文件、执行命令都方便很多。**

---

## 四、服务器安装 Docker

服务器准备好后，下一步就是安装 Docker。

一开始我尝试安装 Docker 官方源，但是遇到了 GPG key / 仓库签名问题。

为了先跑通部署，我改用 Ubuntu 自带的 Docker 包：

```bash
apt install -y docker.io
```

启动 Docker，并设置开机自启：

```bash
systemctl enable --now docker
```

查看 Docker 版本：

```bash
docker --version
```

安装传统版 Docker Compose：

```bash
apt install -y docker-compose
```

查看 Docker Compose 版本：

```bash
docker-compose --version
```

---

### 4.1 Docker Hub 访问超时

第一次运行：

```bash
docker run hello-world
```

时，访问 Docker Hub 超时。

这说明服务器拉取镜像不顺畅，所以我配置了 Docker 镜像加速。

创建配置目录：

```bash
mkdir -p /etc/docker
```

写入镜像加速配置：

```bash
cat > /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io"
  ]
}
EOF
```

重新加载并重启 Docker：

```bash
systemctl daemon-reload
systemctl restart docker
```

再次验证 Docker：

```bash
docker run hello-world
```

看到：

```text
Hello from Docker!
```

说明 Docker 已经可以正常使用。

---

## 五、上传项目文件到服务器

我在服务器上创建统一部署目录：

```bash
mkdir -p /opt/unsky-market
```

这个目录专门用来存放 UnSky Market 的部署文件：

```text
/opt/unsky-market
├── backend.jar
├── frontend
└── init.sql
```

---

### 5.1 上传前端 dist

在 Windows PowerShell 执行：

```bash
scp -r "D:\Develop\UnSky-Market\dist" root@118.31.40.151:/opt/unsky-market/frontend
```

这一步是把前端打包后的 `dist` 上传到服务器，并命名为：

```text
frontend
```

后面 Nginx 会托管这个目录。

---

### 5.2 上传后端 JAR

```bash
scp "D:\Develop\UnSky Market Project\Unsky-backend\target\Unsky-backend-1.0-SNAPSHOT.jar" root@118.31.40.151:/opt/unsky-market/backend.jar
```

这一步是把 Spring Boot 后端打包后的 JAR 上传到服务器。

为了后面命令更简单，我直接把它命名成：

```text
backend.jar
```

---

### 5.3 上传数据库初始化 SQL

```bash
scp "D:\Develop\UnSky Market Project\deploy\mysql\init\001_init.sql" root@118.31.40.151:/opt/unsky-market/init.sql
```

这个 SQL 用来初始化数据库表和测试数据。

---

### 5.4 确认文件是否上传成功

在服务器执行：

```bash
ls -lh /opt/unsky-market
```

看到：

```text
backend.jar
frontend
init.sql
```

说明三个核心文件都已经上传成功。

---

### 5.5 SSH 指纹问题

中间遇到过：

```text
Host key verification failed
```

这个是因为本地保存过旧的服务器 SSH 指纹。

解决方式：

```bash
ssh-keygen -R 118.31.40.151
```

清理旧指纹后，再重新连接即可。

---

## 六、启动 MySQL 容器

数据库使用 MySQL 8.0 容器。

第一次启动命令：

```bash
docker run -d --name unsky-mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=unsky_market \
  -p 3306:3306 \
  -v unsky-mysql-data:/var/lib/mysql \
  -v /opt/unsky-market/init.sql:/docker-entrypoint-initdb.d/init.sql:ro \
  mysql:8.0
```

这条命令做了几件事：

```text
--name unsky-mysql
给 MySQL 容器起名

-e MYSQL_ROOT_PASSWORD=123456
设置 root 密码

-e MYSQL_DATABASE=unsky_market
启动时自动创建 unsky_market 数据库

-p 3306:3306
把服务器 3306 端口映射到容器 3306 端口

-v unsky-mysql-data:/var/lib/mysql
使用 Docker 数据卷保存 MySQL 数据

-v /opt/unsky-market/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
把初始化 SQL 挂载到 MySQL 初始化目录
```

查看日志：

```bash
docker logs unsky-mysql --tail 30
```

看到类似：

```text
Creating database unsky_market
running /docker-entrypoint-initdb.d/init.sql
MySQL init process done
```

说明数据库创建和初始化 SQL 执行成功。

---

## 七、启动 Redis 容器

Redis 启动命令：

```bash
docker run -d --name unsky-redis \
  -p 6379:6379 \
  -v unsky-redis-data:/data \
  redis:7
```

这里的作用是：

```text
启动 Redis 7 容器
映射 6379 端口
使用数据卷保存 Redis 数据
```

测试 Redis：

```bash
docker exec unsky-redis redis-cli ping
```

返回：

```text
PONG
```

说明 Redis 正常运行。

---

## 八、启动 Spring Boot 后端容器

后端使用 JDK/JRE 镜像运行上传的 JAR 文件。

启动命令：

```bash
docker run -d --name unsky-backend \
  --link unsky-mysql:mysql \
  --link unsky-redis:redis \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/unsky_market?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_REDIS_PORT=6379 \
  -v /opt/unsky-market/backend.jar:/app/backend.jar \
  eclipse-temurin:11-jre \
  java -jar /app/backend.jar
```

这里最重要的是这几个点。

---

### 8.1 为什么不能继续用 localhost

在本地开发时，后端连接 MySQL 可以写：

```text
localhost:3306
```

但在 Docker 容器里，`localhost` 指的是：

```text
后端容器自己
```

不是 MySQL 容器。

所以这里通过：

```bash
--link unsky-mysql:mysql
--link unsky-redis:redis
```

给 MySQL 和 Redis 设置别名。

然后后端连接 MySQL 时用：

```text
mysql:3306
```

连接 Redis 时用：

```text
redis:6379
```

---

### 8.2 使用环境变量覆盖配置

这里通过环境变量覆盖 Spring Boot 配置：

```bash
-e SPRING_DATASOURCE_URL=...
-e SPRING_DATASOURCE_USERNAME=root
-e SPRING_DATASOURCE_PASSWORD=123456
-e SPRING_REDIS_HOST=redis
-e SPRING_REDIS_PORT=6379
```

这样不用重新改 JAR 里的配置文件，也能让后端连接容器里的 MySQL 和 Redis。

---

### 8.3 查看后端日志

```bash
docker logs unsky-backend --tail 80
```

看到：

```text
Tomcat started on port(s): 8081
Started UnSkyApplication
```

说明 Spring Boot 后端启动成功。

---

### 8.4 测试后端接口

在服务器内部测试：

```bash
curl http://localhost:8081/api/product/hot?limit=4
```

接口返回数据，说明：

```text
后端启动成功
后端成功连接 MySQL
接口可以正常访问
```

---

## 九、启动 Nginx 前端容器

前端由 Nginx 容器负责。

Nginx 有两个作用：

```text
1. 托管前端 dist 静态页面
2. 把 /api 请求反向代理到后端
```

---

### 9.1 创建 Nginx 配置文件

```bash
cat > /opt/unsky-market/default.conf <<'EOF'
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://unsky-backend:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF
```

这里的关键是：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

这是为了支持前端路由。

比如访问：

```text
/admin/login
/product/1
```

如果没有这个配置，刷新页面可能会 404。

---

### 9.2 /api 反向代理

```nginx
location /api/ {
    proxy_pass http://unsky-backend:8081;
}
```

这表示：

```text
浏览器请求 /api/xxx
  ↓
Nginx 转发给 unsky-backend:8081/api/xxx
```

也就是说，公网用户只访问：

```text
http://118.31.40.151/api/xxx
```

但真正处理请求的是后端容器。

---

### 9.3 启动前端 Nginx 容器

```bash
docker run -d --name unsky-frontend \
  --link unsky-backend:unsky-backend \
  -p 80:80 \
  -v /opt/unsky-market/frontend:/usr/share/nginx/html:ro \
  -v /opt/unsky-market/default.conf:/etc/nginx/conf.d/default.conf:ro \
  nginx:latest
```

这条命令做了几件事：

```text
--name unsky-frontend
创建前端 Nginx 容器

--link unsky-backend:unsky-backend
让 Nginx 能通过 unsky-backend 访问后端容器

-p 80:80
把服务器 80 端口映射到 Nginx 容器 80 端口

-v /opt/unsky-market/frontend:/usr/share/nginx/html:ro
把前端 dist 挂载给 Nginx

-v /opt/unsky-market/default.conf:/etc/nginx/conf.d/default.conf:ro
使用自定义 Nginx 配置
```

---

### 9.4 查看容器状态

```bash
docker ps
```

确认四个容器都在运行：

```text
unsky-frontend
unsky-backend
unsky-redis
unsky-mysql
```

到这里，服务器内部的项目结构已经跑起来了。

---

## 十、开放阿里云安全组

一开始浏览器访问公网 IP 失败。

后来发现问题不是 Docker，也不是 Nginx，而是阿里云安全组没有开放 80 端口。

在阿里云安全组添加入方向规则：

```text
授权策略：允许
协议类型：自定义 TCP
端口范围：80/80
访问来源：0.0.0.0/0
描述：HTTP
```

然后访问：

```text
http://118.31.40.151
```

页面成功展示。

这里我理解到：

```text
服务器里面服务跑起来了，不代表公网就能访问。
公网访问还要看云平台安全组有没有放行端口。
```

---

## 十一、排查 502 问题

部署过程中出现过 502。

我先在服务器内部测试 Nginx 首页：

```bash
curl -I http://localhost
```

返回：

```text
HTTP/1.1 200 OK
```

说明 Nginx 本身可以访问。

然后测试 Nginx 代理接口：

```bash
curl http://localhost/api/product/hot?limit=4
```

也返回了后端数据。

这说明：

```text
服务器内部服务正常
Nginx 正常
后端正常
/api 代理正常
```

所以问题不在 Docker / Nginx / 后端，而是公网访问层。

最终通过开放阿里云安全组 80 端口解决。

---

## 十二、处理中文乱码

页面固定文案是正常的，但是接口返回的数据中文乱码。

一开始我以为是前端问题，但后来判断：

```text
固定文案正常
接口数据乱码
```

说明前端页面和浏览器编码大概率没问题。

问题应该在：

```text
数据库数据导入过程
```

也就是 SQL 文件导入 MySQL 时字符集不对。

---

### 12.1 本机检查 SQL 文件

在本机 PowerShell 检查 SQL 文件里的中文：

```powershell
Select-String -Path "D:\Develop\UnSky Market Project\deploy\mysql\init\001_init.sql" -Pattern "iPhone" -Context 0,2
```

确认本地 SQL 文件中文正常。

---

### 12.2 重新上传 SQL

```bash
scp "D:\Develop\UnSky Market Project\deploy\mysql\init\001_init.sql" root@118.31.40.151:/opt/unsky-market/init.sql
```

---

### 12.3 删除旧 MySQL 容器和数据卷

因为旧数据已经乱码，所以只重传 SQL 不够，需要清空旧数据库数据。

```bash
docker stop unsky-frontend unsky-backend unsky-mysql
docker rm unsky-mysql
docker volume rm unsky-mysql-data
```

这里删除数据卷的作用是：

```text
彻底清掉旧 MySQL 数据
让 MySQL 重新初始化
```

---

### 12.4 重新启动 MySQL，并指定 utf8mb4

```bash
docker run -d --name unsky-mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=unsky_market \
  -p 3306:3306 \
  -v unsky-mysql-data:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

这里明确告诉 MySQL：

```text
服务器字符集使用 utf8mb4
排序规则使用 utf8mb4_unicode_ci
```

---

### 12.5 手动导入 SQL，并指定客户端字符集

```bash
docker exec -i unsky-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 unsky_market < /opt/unsky-market/init.sql
```

这里重点是：

```text
--default-character-set=utf8mb4
```

它表示导入 SQL 时，客户端也按 utf8mb4 读取数据。

---

### 12.6 检查中文是否恢复

```bash
docker exec unsky-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 -e "USE unsky_market; SELECT id,title FROM product LIMIT 3;"
```

看到中文正常后，说明乱码问题解决。

由于旧后端容器 link 的是旧 MySQL 容器，所以后面又删除并重建了后端容器，再重新启动前端容器。

---

## 十三、最终部署结果

最终公网访问地址：

```text
http://118.31.40.151
```

最终部署结构可以理解为：

```text
用户浏览器
  ↓
公网 IP：118.31.40.151
  ↓
阿里云安全组开放 80 端口
  ↓
unsky-frontend Nginx 容器
  ↓
前端 dist 页面
  ↓
/api 请求由 Nginx 反向代理
  ↓
unsky-backend Spring Boot 后端容器
  ↓
MySQL 容器 + Redis 容器
```

这次部署后，UnSky Market 已经可以通过公网 IP 访问。

---

## 十四、我现在对部署的理解

这次部署之后，我对“项目上线”这件事终于有了比较真实的感觉。

以前我理解的项目运行就是：

```text
IDEA 启动后端
npm run dev 启动前端
浏览器访问 localhost
```

但这其实只是本地开发。

真正部署时，前端不是靠 Vite 开发服务器跑，而是先打包成 `dist`，再交给 Nginx 托管。

后端也不是靠 IDEA 启动，而是打包成 JAR，再放到服务器里运行。

MySQL 和 Redis 也不再是我电脑上的服务，而是通过 Docker 容器在服务器里运行。

我现在对整个链路的理解是：

```text
前端页面由 Nginx 返回
/api 请求由 Nginx 转发给后端
后端处理业务逻辑
后端连接 MySQL 和 Redis
云服务器通过公网 IP 暴露访问入口
安全组决定外部能不能访问端口
```

这次也让我明白了，部署不是简单地“把代码传上去”，而是要把整个项目运行需要的环境都安排好。

包括：

```text
前端页面放哪里
后端怎么启动
数据库怎么初始化
Redis 怎么连接
接口怎么转发
端口怎么开放
乱码怎么排查
容器之间怎么通信
```

所以部署其实是在把本地开发环境，重新搭建成一个可以被公网访问的运行环境。

这次踩到的 502、安全组、Docker 镜像、中文乱码问题都挺真实的，也让我真正理解了：

```text
本地能跑，不等于线上能访问。
服务器内部能访问，也不等于公网能访问。
页面能打开，也不等于数据一定正常。
```

现在这个项目已经不只是本地学习项目了，而是真的被我放到了公网环境里。

这一步对我来说算是 UnSky Market 从“开发完成”走向“项目落地”的关键一步。

## 十五、我最后说:┗(｀・ω・´)┛（2026/05/12）

【作者说：在这里做一个简单的小结，或许他作为我的第一个项目不应该这么潦草的结束，但是我也无法给他一个盛大的结尾，所以我决定将他视为开始！(๑˃̵ᴗ˂̵)و 🔥这几天确实拖了很久，但幸运的是最终还是圆满的结束，最近我也在驯服我的codex，准备开始第二阶段的学习了，这几天以及往后几天的任务又会很重，因为我要开始准备期末考试了，压力也而随之上来了(￣▽￣;)🌿 
至此，为期将近24天的学习周期坦然结束，这二十四天里我经历了很多很多，也学到了很多很多，最后仍然更加坚定了不少，我认为在这个阶段我不仅是知识学习层面的进步，还是思想的进化，这很重要！ᕦ(ò_óˇ)ᕤ最后这几天我仍然在这个项目部署上感到乏力和疲惫，的确如此，这个时间线拖了很久很久，我也的确没有精力去写下这篇笔记，所以我将我和`codex`的问答过程学习记录统一交由`codex`老师完成，尽管内容比较潦草，但这也是我的真实学习过程(╯°□°）╯✨
    这个部署我用的是阿里云的学生免费额度，今天也确实学了好久，然后也接触到`linux`，`docker`这些内容，它们也跟我的学习任务密切相关，因为我的计算机网络的报告也是关于它们的！ 👉 (＾▽＾) ✌
我始终认为这24天的旅程不应该就此潦草几句话概括，在这个过程中我进一步有了更加系统的学习手段与思想，我在放弃day11和坚定day11选择了后者，因为我不想后悔，不知是那心中的完美主义作祟，还是心底那不服气的底气所在，少年啊！去做自己想做的！去把它做的更好吧！(๑•̀ㅂ•́)و✧🚀
   学习就像闯关，一路走下去，游戏的主人公会越来越强，以后面对人生的最终大boss也终将会游刃有余！(≧ω≦)  (＾▽＾)  
   至此，我完成了day11的内容，也完成了`UnSky Market`这个项目的全部，但这仍然不是结束，这一阶段的完结是为了下一阶段的开始，在完成之后心中有点空落落的，但更多的是人生得意须尽欢的畅快，我相信往后人生更多的是这样的时刻，我期待着我幻想着...  (｡･ω･｡) 收工收工，最终完工！☁️ 🌊 ⛵已完不续，Skyron，这已经是最终章(⊙ˍ⊙)！！！ 】