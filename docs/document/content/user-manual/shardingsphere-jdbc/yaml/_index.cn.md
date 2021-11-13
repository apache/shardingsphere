+++
title = "YAML"
weight = 2
chapter = true
+++

## 简介

YAML 提供通过配置文件的方式与 ShardingSphere-JDBC 交互。配合治理模块一同使用时，持久化在配置中心的配置均为 YAML 格式。

YAML 配置是最常见的配置方式，可以省略编程的复杂度，简化用户配置。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 配置 YAML

ShardingSphere-JDBC 的 YAML 配置文件通过 Schema 名称、运行模式、数据源集合、规则集合以及属性配置组成。

注：示例的数据库连接池为 HikariCP，可根据业务场景更换为其他数据库连接池。

```yaml
schemaName: my_schema

mode:
  type: Memory

dataSources:
  # 配置第 1 个数据源
  ds_1: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  # 配置第 2 个数据源
  ds_2: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_2
    username: root
    password: 

  # 配置更多数据源
  # ...

rules:
- !FOO_XXX
    ...
- !BAR_XXX
    ...

props:
  key_1: value_1
  key_2: value_2
```

### 构建数据源

通过 YamlShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java

File yamlFile = // 指定 YAML 文件路径
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### 使用数据源

使用方式同 Java API。

## 语法说明

`!!` 表示实例化该类

`!` 表示自定义别名

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
