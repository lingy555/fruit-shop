
# 水果商城系统

## 项目介绍
一个基于Vue 3 + Element Plus的水果商城系统，支持客户端、商家端和管理员端三种角色。

## 技术栈
- **前端框架**: Vue 3.5.26
- **UI组件库**: Element Plus 2.8.8
- **状态管理**: Pinia 2.2.8
- **路由管理**: Vue Router 4.4.5
- **HTTP客户端**: Axios 1.7.9
- **图表库**: ECharts 5.5.0
- **构建工具**: Vite 7.3.0
- **CSS预处理器**: Sass 1.83.1
- **开发语言**: JavaScript

## 项目结构
```
fruit-shop/
├── public/                 # 静态资源
├── src/                   # 源代码
│   ├── api/               # API接口封装
│   ├── assets/            # 样式资源
│   ├── components/         # 公共组件
│   │   ├── common/       # 通用组件
│   │   ├── product/      # 商品相关组件
│   │   └── layout/       # 布局组件
│   ├── config/            # 配置文件
│   ├── router/            # 路由配置
│   ├── store/             # 状态管理
│   │   ├── modules/        # 状态模块
│   ├── utils/             # 工具函数
│   ├── views/             # 页面组件
│   │   ├── admin/          # 管理员端页面
│   │   ├── merchant/        # 商家端页面
│   │   └── customer/       # 客户端页面
│   ├── App.vue             # 根组件
│   ├── main.js            # 入口文件
│   └── .env.development  # 开发环境配置
│   ├── .env.production   # 生产环境配置
│   ├── index.html            # HTML模板
│   ├── jsconfig.json         # JS配置
│   ├── package.json         # 项目依赖
│   ├── package-start.json    # 项目启动脚本
│   └── vite.config.js       # Vite配置
└── dist/                  # 构建输出目录
```

## 功能特性

### 客户端
- 用户注册、登录、密码找回
<img width="1366" height="719" alt="image" src="https://github.com/user-attachments/assets/7a7fb182-9c47-46ba-94fe-0809ece5364c" />
<img width="497" height="590" alt="image" src="https://github.com/user-attachments/assets/ad4dd381-8769-4554-9a46-3cae9da8e0ec" />
- 商品浏览、搜索、筛选、排序
<img width="1490" height="868" alt="image" src="https://github.com/user-attachments/assets/1386fda0-5d36-4905-b128-771020580437" />
<img width="1457" height="670" alt="image" src="https://github.com/user-attachments/assets/83ec3a3b-3c43-4c68-8705-3efc7367ec2e" />
<img width="711" height="558" alt="image" src="https://github.com/user-attachments/assets/b3e268fc-cddc-4542-b0a3-8d24ae2276f2" />
- 商品详情查看、评价
<img width="1444" height="636" alt="image" src="https://github.com/user-attachments/assets/359db652-3ed0-4835-a5cf-58ae4b5efea8" />
<img width="1323" height="621" alt="image" src="https://github.com/user-attachments/assets/a405eff0-5698-47e0-86b4-d6b1b710381f" />
<img width="1091" height="401" alt="image" src="https://github.com/user-attachments/assets/6914332f-e8bb-404e-9d50-1118154ed027" />
- 购物车管理、数量调整、结算
<img width="1448" height="575" alt="image" src="https://github.com/user-attachments/assets/605de111-2050-47d5-a74d-1f61b9648863" />
<img width="1453" height="588" alt="image" src="https://github.com/user-attachments/assets/9c7e1ebc-dc64-462d-a440-48ae4cc67e28" />
<img width="771" height="652" alt="image" src="https://github.com/user-attachments/assets/e04c6cbc-cd50-4829-8f63-99af7234ae01" />
- 订单管理、支付、物流跟踪
<img width="771" height="652" alt="image" src="https://github.com/user-attachments/assets/e04c6cbc-cd50-4829-8f63-99af7234ae01" />
<img width="1179" height="673" alt="image" src="https://github.com/user-attachments/assets/5ecc71a2-c8cb-425a-900f-00ee6d28df62" />
<img width="749" height="608" alt="image" src="https://github.com/user-attachments/assets/293cde69-cc73-4d9a-aad0-14bffb0220db" />

- 收货地址管理、增删改
<img width="1453" height="485" alt="image" src="https://github.com/user-attachments/assets/4c785cd5-990a-4bb7-9023-c20ec336f1fc" />
- 用户中心、个人信息修改
<img width="645" height="414" alt="image" src="https://github.com/user-attachments/assets/60fdda7a-632e-46f2-a755-1d78f75391e1" />


### 商家端
- 店铺信息管理、LOGO上传、营业状态设置
<img width="1609" height="627" alt="image" src="https://github.com/user-attachments/assets/86ce21cd-7966-4aa7-a134-d434fddf926a" />
- 商品管理、上下架
<img width="1506" height="797" alt="image" src="https://github.com/user-attachments/assets/be8731dc-a91b-4a55-a091-0831ff66e4e7" />
<img width="1533" height="130" alt="image" src="https://github.com/user-attachments/assets/0e591161-16d0-42b3-a8b3-125785db4e0f" />
- 订单处理、发货管理、批量操作
<img width="1487" height="644" alt="image" src="https://github.com/user-attachments/assets/35be7210-1e56-4aca-9002-a933d16caa04" />
- 数据统计、销售分析、访客统计
<img width="1579" height="781" alt="image" src="https://github.com/user-attachments/assets/684433bf-8f2c-434b-873d-21ded9764f49" />
- 回复客户
<img width="1521" height="692" alt="image" src="https://github.com/user-attachments/assets/6c58f168-0d0e-4564-bcaa-dfc4f42f8eb5" />
### 管理员端
- 用户管理、信息查看、状态控制
<img width="1193" height="426" alt="image" src="https://github.com/user-attachments/assets/12014fbf-ed81-4c8c-b22b-b9fd111209a9" />
- 商品管理、信息查看、审核管理、状态控制
<img width="1341" height="648" alt="image" src="https://github.com/user-attachments/assets/031369a4-71dc-41f7-9d3c-1cd82dcd228e" />
- 订单管理、全局查看、状态控制
<img width="1373" height="667" alt="image" src="https://github.com/user-attachments/assets/d1030925-2416-457a-8353-4b18027d4dfe" />
- 分类管理、增删改
<img width="1461" height="537" alt="image" src="https://github.com/user-attachments/assets/fa3ff433-299f-4dc2-9aa7-18d15b1a338a" />
- 轮播图管理
<img width="1499" height="436" alt="image" src="https://github.com/user-attachments/assets/7758de61-d7c1-42f4-9383-ab7274fd048b" />
- 优惠券管理、创建发放
- 系统设置、菜单管理、权限配置

## 开发指南

### 环境准备
1. 安装Node.js (建议v18.19.0或更高版本)
2. 安装npm或yarn
3. 配置淘宝镜像源（可选）

### 安装依赖
```bash
npm install
# 或
yarn install
```

### 启动项目
```bash
# 开发环境
npm run dev

# 生产环境
npm run build
```

### 项目配置
- 开发环境配置在 `.env.development`
- 生产环境配置在 `.env.production`

### API配置
- 开发环境API地址：`http://localhost:8080/api`
- 生产环境API地址：`https://api.your-domain.com/api`

## 部署说明

### 构建部署
1. 执行构建命令生成静态文件
2. 将dist目录部署到Web服务器
3. 配置Web服务器（Nginx、Apache等）
4. 配置API代理和跨域

## 注意事项
- 确保API接口地址正确配置
- 生产环境建议使用HTTPS协议
- 定期更新依赖包版本
- 开发时注意浏览器兼容性
- 使用版本控制（Git）进行代码管理

## 联系方式
- 项目问题：请提交Issue到项目仓库
- 技术交流：欢迎提交Pull Request或讨论技术实现
