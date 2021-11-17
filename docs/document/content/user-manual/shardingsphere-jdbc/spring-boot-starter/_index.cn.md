+++
title = "Spring Boot Starter"
weight = 3
chapter = true
+++

## 简介

ShardingSphere-JDBC 提供官方的 Spring Boot Starter，使开发者可以非常便捷的整合 ShardingSphere-JDBC 和 Spring Boot。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 在 Spring 中使用 ShardingSphere 数据源

直接通过注入的方式即可使用 ShardingSphereDataSource；
或者将 ShardingSphereDataSource 配置在 JPA、Hibernate、MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
