+++
title = "Spring Boot Start 配置"
weight = 3
+++

## 简介

ShardingSphere-JDBC 提供官方的 Spring Boot Starter，使开发者可以非常便捷的整合 ShardingSphere-JDBC 和 Spring Boot。

## Spring Boot Start 配置项

### 数据源配置

#### 配置示例

```properties
spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=root

spring.shardingsphere.datasource.ds1.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=root
```

#### 配置项说明

```properties
spring.shardingsphere.datasource.names= # 数据源名称，多数据源以逗号分隔

spring.shardingsphere.datasource.<datasource_name>.type= # 数据库连接池类名称
spring.shardingsphere.datasource.<datasource_name>.driver-class-name= # 数据库驱动类名
spring.shardingsphere.datasource.<datasource_name>.url= # 数据库 URL 连接
spring.shardingsphere.datasource.<datasource_name>.username= # 数据库用户名
spring.shardingsphere.datasource.<datasource_name>.password= # 数据库密码
spring.shardingsphere.datasource.<data-source-name>.xxx= # 数据库连接池的其它属性
```

### 规则配置

#### 配置示例

```properties
spring.shardingsphere.rules.sharding.xxx=xxx
```

#### 配置项说明

```properties
spring.shardingsphere.rules.<rule-type>.xxx= # 规则配置
  # ... 具体的规则配置
```

更多详细配置请参见具体的规则配置部分。

### 属性配置

#### 配置示例

```properties
spring.shardingsphere.props.xxx.xxx=xxx
```

#### 配置项说明

```properties
spring.shardingsphere.props.xxx.xxx= # 具体的属性配置
```
