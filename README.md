# 仁爱医院综合智能化医疗平台 (HMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3.4.0-blue.svg)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-8.0-purple.svg)](https://vitejs.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2-blue.svg)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)

仁爱医院智能化医疗平台（HMS - Hospital Management System）是一款面向现代智慧医院建设的高端全功能综合管理系统。系统基于 **Spring Boot 多模块微服务架构** 与 **Vue 3 + TypeScript + Vite + Element Plus** 现代前端生态链精心打造，深度还原了门诊挂号、就诊开方、线上医保结算、住院登记分配、查房巡诊以及多维运营统计大屏等核心医疗业务流。
---

## 🌟 核心特色与架构设计

### 1. 技术栈大观
* **后端骨架**：基于 Maven 多模块架构，通过 Spring Boot 快速构建起高内聚、低耦合的微服务群。采用 **MyBatis-Plus** 做极速 CRUD，配合 **Redis** 缓存热点排班数据并支持高并发限流。
* **前端生态**：应用 **Vue 3 (Setup SFC)** 配合 **TypeScript** 进行强类型契约绑定，状态管理使用 **Pinia**，路由及守卫使用 **Vue Router 4**，UI 采用 **Element Plus** 暗黑科技风定制。
* **数据持久化**：以 **MySQL 8.0** 为数据核心，利用 Redis 做排队及凭证安全防线。

### 2. 企业级多重安全防线
* **统一网关鉴权**：接口由 `hospital-auth` 提供令牌签发，基于 JWT 机制，自动实现无感 Token 刷新续约。
* **RBAC 角色权限模型**：
  * **系统管理员 (`ROLE_ADMIN`)**：拥有全局科室、医生资质、药品名录录入，以及大屏数据监控统计权。
  * **坐诊医生 (`ROLE_DOCTOR`)**：可查看排班、进行挂号接诊、开具电子处方，并能办理巡诊诊断手续。
  * **就诊患者 (`ROLE_PATIENT`)**：在线预约挂号，通过医保账户/模拟支付进行门诊结算，查询个人住院及历史账单。
* **多维度数据安全**：后端在数据响应时自动利用序列化器拦截器对患者身份证、电话号码、地址等核心敏感字段进行**遮蔽脱敏**；在数据库层面实行数据级权限隔离。

---

## 📁 系统目录结构

```text
hospital-demo/
├── hospital-common/          # 后端公共包：核心工具类、统一异常处理、脱敏序列化器
├── hospital-auth/            # 安全认证模块：JWT 签发、双通道登录验证、Token 续签
├── hospital-system/          # 系统配置模块：科室管理、执业医生、药品目录管理
├── hospital-schedule/        # 医生排班模块：出诊排班表、时段限流
├── hospital-outpatient/      # 门诊业务模块：门诊挂号登记、电子处方开具、医保/现金结算
├── hospital-inpatient/       # 住院业务模块：建立住院档案、分配床位病房、出院结算、巡诊体征
├── hospital-stats/           # 统计分析模块：科室排班热度、医生负荷量、营收多维核算
├── hospital-app/             # 微服务整合打包/聚合启动模块：包含全局 application.yml 配置文件
│
└── hospital-web/             # 强类型 Vue 3 + TypeScript 纯前端工程
    ├── src/
    │   ├── api/              # 严格与后端模块对应的 Axios 统一封装 API 请求包
    │   ├── types/            # DTO、VO、Result 统一强类型接口契约定义 (.d.ts)
    │   ├── utils/            # request.ts 封装（含请求附加 Token、响应 401 自动过期检测）
    │   ├── stores/           # Pinia 状态中心（存储用户 Token、角色权限及应用级配置）
    │   ├── router/           # 包含路由导航守卫，严格根据 RBAC 角色过滤菜单路由
    │   └── views/            # 高端玻璃拟态/科技感深色系 UI 视图界面
    └── vite.config.ts        # Vite 打包配置与 Dev 代理转发配置
```

---

## 🚀 本地开发快速启动

### 第一步：基础软件准备
1. **Java 环境**：确保本地已安装 **JDK 17** 以上版本，并配置好环境变量。
2. **构建工具**：安装配置 **Maven 3.8+**。
3. **数据库**：启动 **MySQL 8.0**，新建数据库 `hospital_db`，并运行附带的 SQL 初始化脚本。
4. **缓存系统**：启动 **Redis 6.x+**，默认端口 `6379`。
5. **前端环境**：安装 **Node.js LTS (v18 或 v20+)**。

### 第二步：后端微服务启动
1. 导入项目到 IntelliJ IDEA 中。
2. 打开 `hospital-app/src/main/resources/application.yml`，修改数据库连接与 Redis 配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/hospital_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
       username: 您的MySQL用户名
       password: 您的MySQL密码
     data:
       redis:
         host: localhost
         port: 6379
         password: 您的Redis密码(若有)
   ```
3. 运行根路径下的 Maven 命令编译项目：
   ```bash
   mvn clean install -DskipTests
   ```
4. 找到 `hospital-app` 服务中的主引导类 `cn.edu.scnu.HospitalApplication` 并点击启动。后端将默认启动在端口 `9080`，并带有 `/api` 的 context-path 前缀。

### 第三步：前端工程启动
1. 终端进入到 `hospital-web` 目录下：
   ```bash
   cd hospital-web
   ```
2. 安装项目依赖：
   ```bash
   npm install
   ```
3. 运行前端开发环境：
   ```bash
   npm run dev
   ```
4. 启动完成后，浏览器访问 `http://localhost:3000` 即可开启体验之旅！
   * **系统管理员账号**：用户名 `admin001` / 密码 `123456`
   * **坐诊医生账号**：工号 `doc001` (内科李明轩) / 密码 `123456`
   * **就诊患者账号**：绑定的手机号 `13900001001` (陈小明) / 密码 `123456`（或者可以通过患者通道进行自助注册新账号）

### 🌟 新增安全与业务功能
1. **多角色专属登录通道**：登录页全新升级为“系统管理员”、“坐诊医生”、“就诊患者”三个专属通道。各角色之间实现严格的身份和权限隔离，防止越权访问。医生账号现已升级为使用专属的 **医生工号**（如 `doc001`）进行登录，更加符合医院正规的系统管理设计。
2. **账号在线状态实时追踪 (`login_status`)**：在 `admin`、`doctor` 和 `patient` 数据库表及对应实体类中均新增了 `login_status` 状态字段（`0=离线，1=在线`）。后端通过在登录时将状态标记为 `1`，在登出或 Token 失效时置回 `0`，实现了对所有角色账户在线/离线状态的精准、实时、动态追踪。
3. **患者自助注册功能**：在患者登录通道下方增加了“立即注册”入口，使用高颜值磨砂玻璃拟态对话框提供流畅的自助注册体验。注册密码以 BCrypt 高强度单向哈希加密存储。
4. **密码算法自适应平滑兼容**：后端认证机制全新重构，自动识别已存在的老旧 MD5 加密测试密码（如 SQL 种子数据）并提供后备检验方案，同时新注册的账户全部采用 BCrypt 安全格式，既提升了安全性又做到了无感向后兼容。

---

## 🖥️ 生产环境企业级部署方案

在生产环境下，为了保证系统的高性能、高内聚和高安全性，推荐使用 **Nginx ➔ 前后端分离部署** 的架构设计。

### 1. 后端微服务打包与运行
在项目根目录下通过 Maven 执行打包：
```bash
mvn clean package -DskipTests
```
打包成功后，将在 `hospital-app/target/` 下生成 `hospital-app.jar`。在服务器上使用后台命令持久化运行：
```bash
nohup java -jar hospital-app.jar --spring.profiles.active=prod > hospital.log 2>&1 &
```

### 2. 前端静态资源编译打包
在 `hospital-web` 目录下运行编译指令：
```bash
npm run build
```
编译成功后，将在 `hospital-web/dist` 目录下输出全部经过混淆与 Tree-shaking 优化的静态 HTML、CSS 和 JS 资源。

### 3. 企业级 Nginx 配置文件示例
将 `dist` 目录上传至服务器（例如 `/usr/share/nginx/hospital/html`）。为了解决**单页应用（SPA）在 HTML5 History 路由模式下，用户手动刷新网页报 404** 的通病，并优雅转发 `/api` 后端接口，提供如下标准的 Nginx 生产配置：

```nginx
server {
    listen       80;
    server_name  hospital.yourdomain.com;
    charset      utf-8;

    # 1. 静态资源托管（开启 gzip 压缩加速）
    location / {
        root   /usr/share/nginx/hospital/html;
        index  index.html index.htm;
        
        # 核心关键：解决 SPA 手动刷新页面 404 问题，全部重定向到 index.html 进行路由分发
        try_files $uri $uri/ /index.html;
        
        # 缓存控制，利于静态资源秒开
        expires 7d;
        add_header Cache-Control "public, no-transform";
    }

    # 2. 接口反向代理转发
    location /api {
        proxy_pass http://127.0.0.1:9080/api;
        
        # 传递真实的用户 IP 和 Host 头以供安全审计
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 延长接口超时防线，防止大宗处方处理中途超时断开
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
        proxy_send_timeout 60s;
    }

    # 3. 开启 Gzip 全链路无感压缩，大幅提升网页加载速率
    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_vary on;

    # 4. 404/50x 错误重定向
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
```

---

## 🔒 核心模块免责声明
本系统仅用于技术演示及学术分享，生产部署时请务必开启 MySQL 与 Redis 的安全鉴权机制，并选用更加强劲的 JWT 密钥配置。
> **仁爱医院研发团队** · 用心呵护每一份健康生命。
