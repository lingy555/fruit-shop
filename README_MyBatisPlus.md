# 水果店项目重构说明

## 概述

本项目已从Spring Data JPA重构为MyBatis Plus + MySQL 8.0 + SSM框架。以下是重构的主要内容和说明。

## 重构内容

### 1. 依赖更新

- 移除了 `spring-boot-starter-data-jpa` 依赖
- 添加了 `mybatis-plus-boot-starter` 依赖
- 添加了 `druid-spring-boot-starter` 连接池依赖

### 2. 配置文件更新

- 更新了 `application.properties`，添加了Druid连接池配置
- 添加了MyBatis Plus相关配置，包括驼峰命名转换、日志实现、ID类型等

### 3. 实体类更新

- 将JPA注解（`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`）替换为MyBatis Plus注解（`@TableName`, `@TableId`, `@TableField`）
- 修改了以下实体类：
  - Product.java
  - CartItem.java
  - User.java（已部分使用MyBatis Plus注解，进行了完善）

### 4. 数据访问层更新

- 创建了 `mapper` 目录，用于存放Mapper接口
- 创建了 `BaseMapper` 接口，继承自MyBatis Plus的 `BaseMapper`
- 创建了以下Mapper接口：
  - ProductMapper.java
  - CartItemMapper.java
  - UserMapper.java

### 5. 主类更新

- 在 `FruitShopApplication` 类中添加了 `@MapperScan("com.lingnan.fruitshop.mapper")` 注解

### 6. 配置类

- 创建了 `MybatisPlusConfig` 配置类，配置了分页插件

## 使用说明

### 1. 分页查询

使用MyBatis Plus的分页插件进行分页查询：

```java
Page<Product> page = new Page<>(1, 10); // 第1页，每页10条
IPage<Product> result = productMapper.selectPage(page, null);
```

### 2. 条件查询

使用QueryWrapper进行条件查询：

```java
QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("status", 1);
List<Product> products = productMapper.selectList(queryWrapper);
```

### 3. 自定义SQL

如需自定义SQL，可以在mapper目录下创建对应的XML文件，例如 `ProductMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lingnan.fruitshop.mapper.ProductMapper">
    <!-- 自定义SQL -->
</mapper>
```

## 后续工作

1. 继续将其他Repository接口转换为Mapper接口
2. 更新Service层代码，适配新的Mapper接口
3. 测试所有功能，确保重构后功能正常

## 注意事项

1. MyBatis Plus的`selectById`方法默认使用实体类的主键字段，而不是数据库列名
2. 使用`@TableField`注解时，如果字段名与列名一致，可以省略value属性
3. MyBatis Plus默认使用实体类字段名进行驼峰命名转换，如`userName`转换为`user_name`
