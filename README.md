
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

- 商品浏览、搜索、筛选、排序
- 商品详情查看、规格选择、评价
- 购物车管理、数量调整、结算
- 订单管理、支付、物流跟踪
- 收货地址管理、增删改
- 用户中心、个人信息修改
- 店铺关注、商品收藏

### 商家端
- 店铺信息管理、LOGO上传、营业状态设置
- 商品管理、上下架、库存管理、规格管理
- 订单处理、发货管理、批量操作
- 数据统计、销售分析、访客统计
- 退款处理、审核管理
- 评价管理、回复处理

### 管理员端
- 管理员管理、角色权限控制
- 商家审核、状态管理、信息查看
- 用户管理、信息查看、状态控制
- 商品管理、信息查看、审核管理、状态控制
- 订单管理、全局查看、状态控制
- 分类管理、增删改
- 内容管理、轮播图管理、公告管理
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
