+++
title = "数据源配置"
weight = 4
chapter = true
+++

## 背景信息

ShardingSphere-Proxy 支持常见的数据库连接池: HikariCP、C3P0、DBCP。

可以通过参数 `dataSourceClassName` 指定连接池，当不指定时，默认的的数据库连接池为 HikariCP。

## 参数解释

```yaml
dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data_source_name>: # 数据源名称
    dataSourceClassName: # 数据源连接池完整类名
    driverClassName: # 数据库驱动类名，以数据库连接池自身配置为准
    jdbcUrl: # 数据库 URL 连接，以数据库连接池自身配置为准
    username: # 数据库用户名，以数据库连接池自身配置为准
    password: # 数据库密码，以数据库连接池自身配置为准
    # ... 数据库连接池的其它属性
```
## 配置示例

```yaml
dataSources:
  ds_1:
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  ds_2:
    dataSourceClassName: com.mchange.v2.c3p0.ComboPooledDataSource
    url: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
  ds_3:
    dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource
    url: jdbc:mysql://localhost:3306/ds_3
    username: root
    password:
  
  # 配置其他数据源
```
