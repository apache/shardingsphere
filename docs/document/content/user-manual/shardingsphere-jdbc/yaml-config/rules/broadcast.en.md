+++
title = "Broadcast Table"
weight = 2
+++

## Background

Broadcast table YAML configuration is highly readable. The broadcast rules can be quickly understood through the YAML format. ShardingSphere automatically creates the ShardingSphereDataSource object according to YAML configuration, which can reduce unnecessary coding for users.

## Parameters

```yaml
rules:
- !BROADCAST
  tables: # Broadcast tables
    - <table_name>
    - <table_name>
```

## Procedure

1. Configure broadcast table list in YAML file.
2. Call createDataSource method of the object YamlShardingSphereDataSourceFactory. Create ShardingSphereDataSource according to the configuration information in YAML files.

## Sample

The YAML configuration sample of broadcast table is as follows:

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !BROADCAST
  tables:
    - t_address
```

Read the YAML configuration to create a data source according to the createDataSource method of YamlShardingSphereDataSourceFactory.

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/broadcast-databases-tables.yaml"));
```
