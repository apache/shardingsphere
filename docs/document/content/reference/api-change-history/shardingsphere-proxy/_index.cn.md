+++
title = "ShardingSphere-Proxy"
weight = 1
chapter = true
+++

## 5.0.0-beta

### 数据源配置项说明

```yaml
schemaName: # 逻辑数据源名称

dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data-source-name>: # 与 ShardingSphere-JDBC 配置不同，无需配置数据库连接池
    url: #数据库 URL 连接
    username: # 数据库用户名
    password: # 数据库密码
    connectionTimeoutMilliseconds: # 连接超时毫秒数
    idleTimeoutMilliseconds: # 空闲连接回收超时毫秒数
    maxLifetimeMilliseconds: # 连接最大存活时间毫秒数
    maxPoolSize: 50 # 最大连接数
    minPoolSize: 1  # 最小连接数     

rules: # 与 ShardingSphere-JDBC 配置一致
  # ...
```

### 权限配置

用于执行登录 Sharding Proxy 的权限验证。 配置用户名、密码、可访问的数据库后，必须使用正确的用户名、密码才可登录。

```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root  # <username>@<hostname>:<password>，hostname 为 % 或空字符串，则代表不限制 host。
      - sharding@:sharding
    provider:
      type: NATIVE  # 必须显式指定
```

hostname 为 % 或空字符串，则代表不限制 host。

provider 的 type 必须显式指定，具体实现可以参考 [5.11 Proxy](https://shardingsphere.apache.org/document/5.0.0-beta/cn/dev-manual/proxy/)

### Proxy 属性

```yaml
props:
  sql-show: # 是否在日志中打印 SQL。打印 SQL 可以帮助开发者快速定位系统问题。日志内容包含：逻辑 SQL，真实 SQL 和 SQL 解析结果。如果开启配置，日志将使用 Topic ShardingSphere-SQL，日志级别是 INFO。
  sql-simple: # 是否在日志中打印简单风格的 SQL。
  executor-size: # 用于设置任务处理线程池的大小。每个 ShardingSphereDataSource 使用一个独立的线程池，同一个 JVM 的不同数据源不共享线程池。
  max-connections-size-per-query: # 一次查询请求在每个数据库实例中所能使用的最大连接数。
  check-table-metadata-enabled: # 是否在程序启动和更新时检查分片元数据的结构一致性。
  proxy-frontend-flush-threshold: # 在 ShardingSphere-Proxy 中设置传输数据条数的 IO 刷新阈值。
  proxy-transaction-type: # ShardingSphere-Proxy 中使用的默认事务类型。包括：LOCAL、XA 和 BASE。
  proxy-opentracing-enabled: # 是否允许在 ShardingSphere-Proxy 中使用 OpenTracing。
  proxy-hint-enabled: # 是否允许在 ShardingSphere-Proxy 中使用 Hint。使用 Hint 会将 Proxy 的线程处理模型由 IO 多路复用变更为每个请求一个独立的线程，会降低 Proxy 的吞吐量。
  xa-transaction-manager-type: # XA 事务管理器类型。列如：Atomikos，Narayana，Bitronix。
```

## 5.0.0-alpha

### 数据源配置项说明

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

#### 覆盖 dataSourceCommon 说明

上面配置了每个库的公共数据源配置，如果你想覆盖 dataSourceCommon 属性，请在每个数据源单独配置。

```yaml
dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data-source-name>: # 与 ShardingSphere-JDBC 配置不同，无需配置数据库连接池
    url: # 数据库 URL 连接
    username: # 数据库用户名，覆盖 dataSourceCommon 配置
    password: # 数据库密码，覆盖 dataSourceCommon 配置
    connectionTimeoutMilliseconds: # 连接超时毫秒数，覆盖 dataSourceCommon 配置
    idleTimeoutMilliseconds: # 空闲连接回收超时毫秒数，覆盖 dataSourceCommon 配置
    maxLifetimeMilliseconds: # 连接最大存活时间毫秒数，覆盖 dataSourceCommon 配置
    maxPoolSize: # 最大连接数，覆盖 dataSourceCommon 配置
```

#### 权限配置

用于执行登录 Sharding Proxy 的权限验证。 配置用户名、密码、可访问的数据库后，必须使用正确的用户名、密码才可登录。

```yaml
authentication:
  users:
    root: # 自定义用户名
      password: root # 自定义用户名
    sharding: # 自定义用户名
      password: sharding # 自定义用户名
      authorizedSchemas: sharding_db, replica_query_db # 该用户授权可访问的数据库，多个用逗号分隔。缺省将拥有 root 权限，可访问全部数据库。
```

#### Proxy 属性

```yaml
props:
  sql-show: # 是否在日志中打印 SQL。打印 SQL 可以帮助开发者快速定位系统问题。日志内容包含：逻辑 SQL，真实 SQL 和 SQL 解析结果。如果开启配置，日志将使用 Topic ShardingSphere-SQL，日志级别是 INFO。
  sql-simple: # 是否在日志中打印简单风格的 SQL。
  acceptor-size: # 用于设置接收 TCP 请求线程池的大小。
  executor-size: # 用于设置任务处理线程池的大小。每个 ShardingSphereDataSource 使用一个独立的线程池，同一个 JVM 的不同数据源不共享线程池。
  max-connections-size-per-query: # 一次查询请求在每个数据库实例中所能使用的最大连接数。
  check-table-metadata-enabled: # 是否在程序启动和更新时检查分片元数据的结构一致性。
  query-with-cipher-column: # 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询。
  proxy-frontend-flush-threshold: # 在 ShardingSphere-Proxy 中设置传输数据条数的 IO 刷新阈值。
  proxy-transaction-type: # ShardingSphere-Proxy 中使用的默认事务类型。包括：LOCAL、XA 和 BASE。
  proxy-opentracing-enabled: # 是否允许在 ShardingSphere-Proxy 中使用 OpenTracing。
  proxy-hint-enabled: # 是否允许在 ShardingSphere-Proxy 中使用 Hint。使用 Hint 会将 Proxy 的线程处理模型由 IO 多路复用变更为每个请求一个独立的线程，会降低 Proxy 的吞吐量。
```

## ShardingSphere-4.x

### 数据源与分片配置项说明

#### 数据分片

```yaml
schemaName: # 逻辑数据源名称

dataSources: # 数据源配置，可配置多个 data_source_name
  <data_source_name>: # 与 Sharding-JDBC 配置不同，无需配置数据库连接池
    url: # 数据库 url 连接
    username: # 数据库用户名
    password: # 数据库密码
    connectionTimeoutMilliseconds: 30000 # 连接超时毫秒数
    idleTimeoutMilliseconds: 60000 # 空闲连接回收超时毫秒数
    maxLifetimeMilliseconds: 1800000 # 连接最大存活时间毫秒数
    maxPoolSize: 65 # 最大连接数

shardingRule: # 省略数据分片配置，与 Sharding-JDBC 配置一致
```

#### 读写分离

```yaml
schemaName: # 逻辑数据源名称

dataSources: # 省略数据源配置，与数据分片一致

masterSlaveRule: # 省略读写分离配置，与 Sharding-JDBC 配置一致
```

#### 数据脱敏

```yaml
dataSource: # 省略数据源配置

encryptRule:
  encryptors:
    <encryptor-name>:
      type: # 加解密器类型，可自定义或选择内置类型：MD5/AES 
      props: # 属性配置, 注意：使用 AES 加密器，需要配置 AES 加密器的KEY属性：aes.key.value
        aes.key.value:
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: # 存储明文的字段
          cipherColumn: # 存储密文的字段
          assistedQueryColumn: # 辅助查询字段，针对 ShardingQueryAssistedEncryptor 类型的加解密器进行辅助查询
          encryptor: # 加密器名字
props:
  query.with.cipher.column: true # 是否使用密文列查询
```

### 全局配置项说明

#### 治理

与 Sharding-JDBC 配置一致。

#### Proxy 属性

```yaml
# 省略与 Sharding-JDBC 一致的配置属性

props:
  acceptor.size: # 用于设置接收客户端请求的工作线程个数，默认为 CPU 核数 *2
  proxy.transaction.type: # 默认为 LOCAL 事务，允许 LOCAL，XA，BASE 三个值，XA 采用 Atomikos 作为事务管理器，BASE 类型需要拷贝实现 ShardingTransactionManager 的接口的 jar 包至 lib 目录中
  proxy.opentracing.enabled: # 是否开启链路追踪功能，默认为不开启。详情请参见[链路追踪](/cn/features/orchestration/apm/)
  check.table.metadata.enabled: # 是否在启动时检查分表元数据一致性，默认值: false
  proxy.frontend.flush.threshold: # 对于单个大查询,每多少个网络包返回一次
```

#### 权限验证

用于执行登录 Sharding Proxy 的权限验证。配置用户名、密码、可访问的数据库后，必须使用正确的用户名、密码才可登录 Proxy。

```yaml
authentication:
  users:
    root: # 自定义用户名
      password: root # 自定义用户名
    sharding: # 自定义用户名
      password: sharding # 自定义用户名
      authorizedSchemas: sharding_db, masterslave_db # 该用户授权可访问的数据库，多个用逗号分隔。缺省将拥有 root 权限，可访问全部数据库。
```

## ShardingSphere-3.x

### 数据源与分片配置项说明

#### 数据分片

```yaml
schemaName: # 逻辑数据源名称

dataSources: # 数据源配置，可配置多个 data_source_name
  <data_source_name>: # 与 Sharding-JDBC 配置不同，无需配置数据库连接池
    url: # 数据库url连接
    username: # 数据库用户名
    password: # 数据库密码
    autoCommit: true # hikari连接池默认配置
    connectionTimeout: 30000 # hikari 连接池默认配置
    idleTimeout: 60000 # hikari 连接池默认配置
    maxLifetime: 1800000 # hikari 连接池默认配置
    maximumPoolSize: 65 # hikari 连接池默认配置

shardingRule: # 省略数据分片配置，与 Sharding-JDBC 配置一致
```

#### 读写分离

```yaml
schemaName: # 逻辑数据源名称

dataSources: # 省略数据源配置，与数据分片一致

masterSlaveRule: # 省略读写分离配置，与 Sharding-JDBC 配置一致
```

### 全局配置项说明

#### 数据治理

与 Sharding-JDBC 配置一致。

#### Proxy 属性

```yaml
# 省略与 Sharding-JDBC 一致的配置属性

props:
  acceptor.size: # 用于设置接收客户端请求的工作线程个数，默认为 CPU 核数 *2
  proxy.transaction.enabled: # 是否开启事务, 目前仅支持XA事务，默认为不开启
  proxy.opentracing.enabled: # 是否开启链路追踪功能，默认为不开启。详情请参见[链路追踪](/cn/features/orchestration/apm/)
  check.table.metadata.enabled: # 是否在启动时检查分表元数据一致性，默认值: false
```

#### 权限验证

用于执行登录 Sharding Proxy 的权限验证。配置用户名、密码后，必须使用正确的用户名、密码才可登录 Proxy。

```yaml
authentication:
  username: root
  password:
```
