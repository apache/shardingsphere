+++
title = "数据源配置"
weight = 2
chapter = true
+++

## 背景信息

ShardingSphere-JDBC 支持所有的数据库 JDBC 驱动和连接池。

示例的数据库驱动为 MySQL，连接池为 HikariCP，可以更换为其他数据库驱动和连接池。当使用 ShardingSphere-JDBC 时，JDBC 池的属性名取决于各自 JDBC 池自己的定义，并不由 ShardingSphere 定义，相关的处理可以参考类`org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator`。例如对于 Alibaba Druid 1.2.9 而言，使用 `url` 代替如下示例中的 `standardJdbcUrl` 是预期行为。

## 参数解释

```yaml
dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data_source_name>: # 数据源名称
    dataSourceClassName: # 数据源完整类名
    driverClassName: # 数据库驱动类名，以数据库连接池自身配置为准
    standardJdbcUrl: # 数据库 URL 连接，以数据库连接池自身配置为准
    username: # 数据库用户名，以数据库连接池自身配置为准
    password: # 数据库密码，以数据库连接池自身配置为准
    # ... 数据库连接池的其它属性
```
## 配置示例

```yaml
dataSources:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
  
  # 配置其他数据源
```
