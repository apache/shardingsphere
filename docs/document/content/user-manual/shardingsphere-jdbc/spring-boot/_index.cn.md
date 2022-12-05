+++
title = "Spring Boot"
weight = 4
chapter = true
+++

## 简介

ShardingSphere 提供 JDBC 驱动，开发者可以在 Spring Boot 中配置 `ShardingSphereDriver` 来使用 ShardingSphere。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 配置 Spring Boot

#### 驱动类名称

`org.apache.shardingsphere.driver.ShardingSphereDriver`

#### URL 配置说明

- 以 `jdbc:shardingsphere:` 为前缀
- 配置文件：`xxx.yaml`，配置文件格式与 [YAML 配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/)一致
- 配置文件加载规则：
  - 无前缀表示从指定路径加载配置文件
  - `classpath:` 前缀表示从类路径中加载配置文件

```properties
# 配置 DataSource Driver
spring.datasource.driver-class-name=org.apache.shardingsphere.driver.ShardingSphereDriver
# 指定 YAML 配置文件
spring.datasource.url=jdbc:shardingsphere:classpath:xxx.yaml
```

### 使用数据源

直接使用该数据源；或者将 ShardingSphereDataSource 配置在 JPA、Hibernate、MyBatis 等 ORM 框架中配合使用。
