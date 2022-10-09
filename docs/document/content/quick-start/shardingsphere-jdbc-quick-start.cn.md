+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## 应用场景

Apache ShardingSphere-JDBC 可以通过 `Java`，`YAML`，`Spring 命名空间` 和 `Spring Boot Starter` 这 4 种方式进行配置，开发者可根据场景选择适合的配置方式。

## 使用限制

目前仅支持 JAVA 语言

## 前提条件

开发环境需要具备 Java JRE 8 或更高版本。

## 操作步骤


1. 规则配置。

详情请参见[用户手册](/cn/user-manual/shardingsphere-jdbc/)。

2. 引入 maven 依赖。

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> 注意：请将 `${latest.release.version}` 更改为实际的版本号。



3. 编辑 `application.yml`。



```java
spring:
  shardingsphere:
    datasource:
      names: ds_0, ds_1
      ds_0:
        type: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.mysql.cj.jdbc.Driver
        jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password: 
      ds_1:
        type: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.mysql.cj.jdbc.Driver
        jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password: 
    rules:
      sharding:
        tables:
            ...
```
