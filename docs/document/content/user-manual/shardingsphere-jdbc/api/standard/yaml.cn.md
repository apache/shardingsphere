+++
title = "使用 YAML 配置"
weight = 2
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 构建数据源

### YAML 配置

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

rules:
- !FOO_XXX
    ...
- !BAR_XXX
    ...

props:
  key_1: value_1
  key_2: value_2
```

### 构建 ShardingSphere 数据源

```java
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

## 使用 ShardingSphere 数据源

使用方式同 Java API。
