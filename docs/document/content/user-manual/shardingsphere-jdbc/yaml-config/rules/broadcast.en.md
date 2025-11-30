+++
title = "Broadcast Table"
weight = 2
+++

## Background

Broadcast table YAML configuration is highly readable. The broadcast rules can be quickly understood thanks to the YAML format. ShardingSphere automatically creates the `ShardingSphereDataSource` object according to the YAML configuration, which reduces unnecessary coding for users.

## Parameters

```yaml
rules:
- !BROADCAST
  tables: # Broadcast tables
    - <table_name>
    - <table_name>
```

## Procedure

1. Configure broadcast table list in the YAML file.
2. Call the `createDataSource` method of the object `YamlShardingSphereDataSourceFactory`. Create ShardingSphereDataSource according to the configuration information in YAML files.

## Sample

The YAML configuration sample of the broadcast table is as follows:

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !BROADCAST
  tables:
    - t_address
```

Read the YAML configuration to create a data source according to the `createDataSource` method of `YamlShardingSphereDataSourceFactory`.

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/broadcast-databases-tables.yaml"));
```
