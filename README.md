# SmartNote

一个基于 Java 编写的 SpringBoot + Mybatis Plus + Spring AI 的智能笔记应用，支持文件夹式笔记管理、多人实时编辑、好友系统和在线聊天功能，采用前后端分离架构

## 一、主要功能

**笔记编辑**

- 使用 Milkdown 所见即所得 Markdown 编辑器，支持实时预览
- 文件夹树状管理，支持标签、搜索与其他基础增删改查
- 回收站定期清理

**实时协作**

- 多人同时编辑同一篇笔记，WebSocket 广播同步
- 显示当前在线编辑人数
- 支持对单个好友设置笔记权限

**AI 助手**

- AI 助手功能，使用智能路由并支持三种模式：闲聊、笔记摘要、知识检索
- AI 可自主调用工具搜索和读取你的笔记，基于向量语义检索
- 可以对单篇笔记发起 AI 分析请求，并持久化到数据库

**社交**

- 好友系统：添加/删除好友，好友分组
- 私聊：WebSocket 实时消息，支持未读提醒和已读
- 笔记权限：可指定好友只读或可编辑

## 二、技术栈

|层|技术|
| ----------| -----------------------------------------------------------------|
|后端|Java 17, Spring Boot 3.4, MyBatis-Plus, Sa-Token, Caffeine 缓存|
|前端|Vue 3, Element Plus, Milkdown, SaToken|
|数据库|MySQL 8|
|AI|Spring AI|
|实时通信|WebSocket|

## 三、快速开始

### 环境要求

- Java 17+
- Node.js \>\= 20.19 或 \>\= 22.12
- MySQL 8.x

### 1. 数据库

建表脚本在 `backend/src/main/resources/db/table.sql`，直接运行即可

### 2. 环境变量

|变量|说明|
| --------| ----------------|
|​`DB_URL`|MySQL 连接地址|
|​`DB_USERNAME`|数据库用户名|
|​`DB_PASSWORD`|数据库密码|
|​`API_KEY`<br />|AI api key|

### 3. 启动后端

```
 cd backend
 ./mvnw spring-boot:run
```

默认跑在 8081 端口。

### 4. 启动前端

```
 cd fronted
 npm install
 npm run dev
```

浏览器打开 `http://localhost:5173` 即可。

## 四、项目结构

```
 backend/
   src/main/java/hiiii113/smartnote/
     controller/       # 接口
     service/          # 业务逻辑
     mapper/           # 数据访问层（MyBatis-Plus）
     entity/           # 实体类
     config/           # Sa-Token、WebSocket、缓存等配置
     websocket/        # 协作编辑 + 聊天的 WebSocket Handler
     tools/            # AI 可调用的笔记工具
 
 fronted/
   src/
     views/
       Login.vue       # 登录注册
       Note.vue        # 主编辑页面
       Chat.vue        # 聊天
       Friends.vue     # 好友管理
     components/
       MilkdownEditor.vue  # Milkdown 编辑器封装
     stores/           # Pinia 状态管理
     utils/            # Axios 封装、WebSocket 工具
```

## 五、配置

AI 和 向量数据库所用模型自行在`backend/src/main/resources/application.yml`文件中修改即可

## 许可

MIT

‍
