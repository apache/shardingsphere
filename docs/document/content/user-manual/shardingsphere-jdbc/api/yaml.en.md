+++
title = "Use YAML"
weight = 2
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Build Data Source

### YAML Configuration

ShardingSphere-JDBC YAML file consists of schema name, mode configuration, data source map, rule configurations and properties.

Note: The example connection pool is HikariCP, which can be replaced with other connection pools according to business scenarios.

```yaml
schemaName: my_schema

mode:
  type: Memory

dataSources:
  # Configure 1st data source
  ds_1: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  # Configure nth data source
  ds_n: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_n
    username: root
    password:

rules:
  - !XXX_1
    ...
  - !XXX_N
    ...

props:
  key_1: value_1
  key_n: value_n
```

### Build ShardingSphere Data Source

```java
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

## Use ShardingSphere Data Source

Same with Java API.
