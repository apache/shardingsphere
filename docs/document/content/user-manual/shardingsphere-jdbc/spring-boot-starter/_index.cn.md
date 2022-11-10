+++
title = "Spring Boot Starter"
weight = 4
chapter = true
+++

## 简介

ShardingSphere-JDBC 提供官方的 Spring Boot Starter，使开发者可以非常便捷的整合 ShardingSphere-JDBC 和 Spring Boot。

兼容 SpringBoot 版本支持列表如下 : 

1. SpringBoot 1.x
2. SpringBoot 2.x 
3. SpringBoot 3.x (实验中)

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 配置 Spring Boot 属性

ShardingSphere-JDBC 的 Spring Boot 属性配置由 Database 名称、运行模式、数据源集合、规则集合以及属性配置组成。

```properties
# JDBC 逻辑库名称。在集群模式中，使用该参数来联通 ShardingSphere-JDBC 与 ShardingSphere-Proxy。
spring.shardingsphere.database.name= # 逻辑库名称，默认值：logic_db
spring.shardingsphere.mode.xxx= # 运行模式
spring.shardingsphere.dataSource.xxx= # 数据源集合
spring.shardingsphere.rules.xxx= # 规则集合
spring.shardingsphere.props= # 属性配置
```

模式详情请参见[模式配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/mode)。

数据源详情请参见[数据源配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/data-source)。

规则详情请参见[规则配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules)。

### 使用数据源

直接通过注入的方式即可使用 ShardingSphereDataSource；
或者将 ShardingSphereDataSource 配置在 JPA、Hibernate、MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
