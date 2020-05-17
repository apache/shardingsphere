+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## 1. 引入maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

注意: 请将`${latest.release.version}`更改为实际的版本号。

## 2. 规则配置

ShardingSphere-JDBC可以通过`Java`，`YAML`，`Spring命名空间`和`Spring Boot Starter`四种方式配置，开发者可根据场景选择适合的配置方式。详情请参见[配置手册](/cn/manual/shardingsphere-jdbc/configuration/)。

## 3. 创建DataSource

通过ShardingDataSourceFactory工厂和规则配置对象获取ShardingDataSource，ShardingDataSource实现自JDBC的标准接口DataSource。然后即可通过DataSource选择使用原生JDBC开发，或者使用JPA, MyBatis等ORM工具。

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, props);
```
