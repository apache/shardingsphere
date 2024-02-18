+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## 应用场景

Apache ShardingSphere-JDBC 可以通过 `Java` 和 `YAML` 这 2 种方式进行配置，开发者可根据场景选择适合的配置方式。

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
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> 注意：请将 `${latest.release.version}` 更改为实际的版本号。

3. 创建 YAML 配置文件

```yaml
# JDBC 逻辑库名称。在集群模式中，使用该参数来联通 ShardingSphere-JDBC 与 ShardingSphere-Proxy。
# 默认值：logic_db
databaseName (?):

mode:

dataSources:

rules:
- !FOO_XXX
    ...
- !BAR_XXX
    ...

props:
  key_1: value_1
  key_2: value_2
```

4. 以 `spring boot` 为例，编辑 `application.properties`。

```properties
# 配置 DataSource Driver
spring.datasource.driver-class-name=org.apache.shardingsphere.driver.ShardingSphereDriver
# 指定 YAML 配置文件
spring.datasource.url=jdbc:shardingsphere:classpath:xxx.yaml
```

详情请参见[Spring Boot](/cn/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/spring-boot/)。
