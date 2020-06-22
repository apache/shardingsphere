+++
title = "Spring Boot Start 配置"
weight = 3
+++

## 简介

ShardingSphere-JDBC 提供官方的 Spring Boot Starter，使开发者可以非常便捷的整合 ShardingSphere-JDBC 和 Spring Boot。

## 数据源配置

```properties
spring.shardingsphere.datasource.names= # 数据源名称，多数据源以逗号分隔

spring.shardingsphere.datasource.common.type=  # 数据库连接池类名称
spring.shardingsphere.datasource.common.driver-class-name= # 数据库驱动类名
spring.shardingsphere.datasource.common.username= # 数据库用户名
spring.shardingsphere.datasource.common.password= # 数据库密码
spring.shardingsphere.datasource.common.xxx=  # 数据库连接池的其它属性

spring.shardingsphere.datasource.<datasource-name>.url= # 数据库 URL 连接
```

## 覆盖数据源 common 配置
```properties
spring.shardingsphere.datasource.<datasource-name>.url= # 数据库 URL 连接
spring.shardingsphere.datasource.<datasource-name>.type= # 数据库连接池类名称，覆盖 common 中的 type 配置
spring.shardingsphere.datasource.<datasource-name>.driver-class-name= # 数据库驱动类名，覆盖 common 中的 driver-class-name 配置
spring.shardingsphere.datasource.<datasource-name>.username= # 数据库用户名 ，覆盖 common 中的 username 配置
spring.shardingsphere.datasource.<datasource-name>.password= # 数据库密码 ，覆盖 common 中的 password 配置
spring.shardingsphere.datasource.<data-source-name>.xxx= # 数据库连接池的其它属性 ，覆盖 common 中其他属性配置
```


## 规则配置

```properties
spring.shardingsphere.rules.<rule-type>.xxx= # 规则配置
  # ... 具体的规则配置
```

更多详细配置请参见具体的规则配置部分。

## 属性配置

```properties
spring.shardingsphere.props.xxx.xxx= # 具体的属性配置
```

详情请参见[属性配置](/cn/user-manual/shardingsphere-jdbc/configuration/props)。
