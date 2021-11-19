+++
title = "数据源配置"
weight = 2
chapter = true
+++

数据源配置分为单数据源配置和多数据源配置。ShardingSphere-JDBC 支持所有的数据库 JDBC 驱动和连接池。

示例的数据库驱动为 MySQL，连接池为 HikariCP，可以更换为其他数据库驱动和连接池。

## 单数据源配置

用于数据加密等规则。

### 配置项说明

```yaml
dataSource: # <!!数据库连接池实现类> `!!`表示实例化该类
  driverClassName: # 数据库驱动类名，以数据库连接池自身配置为准
  url: # 数据库 URL 连接，以数据库连接池自身配置为准
  username: # 数据库用户名，以数据库连接池自身配置为准
  password: # 数据库密码，以数据库连接池自身配置为准
  # ... 数据库连接池的其它属性
```

### 配置示例

```yaml
dataSource: !!com.zaxxer.hikari.HikariDataSource
  driverClassName: com.mysql.jdbc.Driver
  jdbcUrl: jdbc:mysql://localhost:3306/ds
  username: root
  password:
```

## 多数据源配置

用于分片、读写分离等规则。
如果加密和分片等功能混合使用，则应使用多数据源配置。

### 配置项说明

```yaml
dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data-source-name>: # <!!数据库连接池实现类>，`!!` 表示实例化该类
    driverClassName: # 数据库驱动类名，以数据库连接池自身配置为准
    url: # 数据库 URL 连接，以数据库连接池自身配置为准
    username: # 数据库用户名，以数据库连接池自身配置为准
    password: # 数据库密码，以数据库连接池自身配置为准
    # ... 数据库连接池的其它属性
```

### 配置示例

```yaml
dataSources:
  ds_1: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  ds_2: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
  
  # 配置其他数据源
```
