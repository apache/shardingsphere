+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## 1. 引入 maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

注意：请将 `${latest.release.version}` 更改为实际的版本号。

## 2. 规则配置

ShardingSphere-JDBC 可以通过 `Java`，`YAML`，`Spring 命名空间`和 `Spring Boot Starter` 这 4 种方式进行配置，开发者可根据场景选择适合的配置方式。
详情请参见[配置手册](/cn/user-manual/shardingsphere-jdbc/configuration/)。

## 3. 创建数据源

通过 `ShardingSphereDataSourceFactory` 工厂和规则配置对象获取 `ShardingSphereDataSource`。
该对象实现自 JDBC 的标准 DataSource 接口，可用于原生 JDBC 开发，或使用 JPA, MyBatis 等 ORM 类库。

```java
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, configurations, properties);
```
