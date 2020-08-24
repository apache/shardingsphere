+++
title = "数据源配置"
weight = 1
+++

## 配置项说明

```yaml
schemaName: # 逻辑数据源名称
dataSourceCommon:
  username: # 数据库用户名
  password: # 数据库密码
  connectionTimeoutMilliseconds: # 连接超时毫秒数
  idleTimeoutMilliseconds: # 空闲连接回收超时毫秒数
  maxLifetimeMilliseconds: # 连接最大存活时间毫秒数
  maxPoolSize: 50 # 最大连接数
  minPoolSize: 1  # 最小连接数

dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data-source-name>: # 与 ShardingSphere-JDBC 配置不同，无需配置数据库连接池
    url: #数据库 URL 连接
rules: # 与 ShardingSphere-JDBC 配置一致
  # ...
```

## 覆盖dataSourceCommon说明

上面配置了每个库的公共数据源配置，如果你想覆盖 `dataSourceCommon` 属性，请在每个数据源单独配置。

```yaml
dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data-source-name>: # 与 ShardingSphere-JDBC 配置不同，无需配置数据库连接池
    url: #数据库 URL 连接
    username: # 数据库用户名，覆盖 dataSourceCommon 配置
    password: # 数据库密码，覆盖 dataSourceCommon 配置
    connectionTimeoutMilliseconds: # 连接超时毫秒数，覆盖 dataSourceCommon 配置
    idleTimeoutMilliseconds: # 空闲连接回收超时毫秒数，覆盖 dataSourceCommon 配置
    maxLifetimeMilliseconds: # 连接最大存活时间毫秒数，覆盖 dataSourceCommon 配置
    maxPoolSize: # 最大连接数，覆盖 dataSourceCommon 配置
```
