+++
pre = "<b>4.2.2. </b>"
toc = true
title = "配置手册"
weight = 2
+++

## 数据源与分片配置示例

Sharding-Proxy支持多逻辑数据源，每个以config-前缀命名的yaml配置文件，即为一个逻辑数据源。以下是`config-xxx.yaml`的配置配置示例。

### 数据分片

dataSources:

```yaml
schemaName: sharding_db

dataSources:
  ds0: 
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds1:
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds${0..1}.t_order${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        columnn: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
```

### 读写分离

```yaml
schemaName: master_slave_db

dataSources:
  ds_master:
    url: jdbc:mysql://localhost:3306/ds_master
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds_slave0:
    url: jdbc:mysql://localhost:3306/ds_slave0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds_slave1:
    url: jdbc:mysql://localhost:3306/ds_slave1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave0
    - ds_slave1
```

### 数据分片 + 读写分离

```yaml
schemaName: sharding_master_slave_db

dataSources:
  ds0:
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds0_slave0:
    url: jdbc:mysql://localhost:3306/ds0_slave0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds0_slave1:
    url: jdbc:mysql://localhost:3306/ds0_slave1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds1:
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds1_slave0:
    url: jdbc:mysql://localhost:3306/ds1_slave0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000
  ds1_slave1:
    url: jdbc:mysql://localhost:3306/ds1_slave1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ms_ds${0..1}.t_order${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        column: order_id
    t_order_item:
      actualDataNodes: ms_ds${0..1}.t_order_item${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ms_ds${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
  
  masterSlaveRules:
      ms_ds0:
        masterDataSourceName: ds0
        slaveDataSourceNames:
          - ds0_slave0
          - ds0_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
      ms_ds1:
        masterDataSourceName: ds1
        slaveDataSourceNames: 
          - ds1_slave0
          - ds1_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
```

## 全局配置示例

Sharding-Proxy使用conf/server.yaml配置注册中心、认证信息以及公用属性。

### 数据治理

```yaml
#省略数据分片和读写分离配置

orchestration:
  name: orchestration_ds
  overwrite: true
  registry:
    namespace: orchestration
    serverLists: localhost:2181
```

### 认证信息

```yaml
authentication:
  username: root
  password:
```

### 公用属性

```yaml
props:
  executor.size: 16
  sql.show: false
```

## 数据源与分片配置项说明

### 数据分片

```yaml
schemaName: #逻辑数据源名称

dataSources: #数据源配置，可配置多个data_source_name
  <data_source_name>: #与Sharding-JDBC配置不同，无需配置数据库连接池
    url: #数据库url连接
    username: #数据库用户名
    password: #数据库密码
    connectionTimeoutMilliseconds: 30000 #连接超时毫秒数
    idleTimeoutMilliseconds: 60000 #空闲连接回收超时毫秒数
    maxLifetimeMilliseconds: 1800000 #连接最大存活时间毫秒数
    maxPoolSize: 65 #最大连接数
    minPoolSize: 1 #最小连接数
    maintenanceIntervalMilliseconds: 30000 #监控检测间隔毫秒数 

shardingRule: #省略数据分片配置，与Sharding-JDBC配置一致
```

### 读写分离

```yaml
schemaName: #逻辑数据源名称

dataSources: #省略数据源配置，与数据分片一致

masterSlaveRule: #省略读写分离配置，与Sharding-JDBC配置一致
```

## 全局配置项说明

### 数据治理

与Sharding-JDBC配置一致。

### Proxy属性

```yaml
#省略与Sharding-JDBC一致的配置属性
props:
  acceptor.size: #用于设置接收客户端请求的工作线程个数，默认为CPU核数*2
  proxy.transaction.enabled: #是否开启事务, 目前仅支持XA事务，默认为不开启
  proxy.opentracing.enabled: #是否开启链路追踪功能，默认为不开启。详情请参见[链路追踪](/cn/features/orchestration/apm/)
  check.table.metadata.enabled: #是否在启动时检查分表元数据一致性，默认值: false
```

### 权限验证

用于执行登录Sharding Proxy的权限验证。配置用户名、密码后，必须使用正确的用户名、密码才可登录Proxy。

```yaml
authentication:
   username: root
   password:
```

## Yaml语法说明

`!!` 表示实例化该类

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
