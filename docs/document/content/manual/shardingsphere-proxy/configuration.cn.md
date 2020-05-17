+++
pre = "<b>4.2.2. </b>"
title = "配置手册"
weight = 2
+++

## 数据源与分片配置示例

ShardingSphere-Proxy支持多逻辑数据源，每个以config-前缀命名的yaml配置文件，即为一个逻辑数据源。以下是`config-xxx.yaml`的配置配置示例。

### 数据分片

dataSources:

```yaml
schemaName: sharding_db

dataSources:
  ds0: 
    url: jdbc:postgresql://localhost:5432/ds0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds1:
    url: jdbc:postgresql://localhost:5432/ds1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65

shardingRule:
  tables:
    t_order:
      actualDataNodes: ds${0..1}.t_order${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_item_id
  bindingTables:
    - t_order,t_order_item
  defaultTableStrategy:
    none:
```

### 读写分离

```yaml
schemaName: master_slave_db

dataSources:
  ds_master:
    url: jdbc:postgresql://localhost:5432/ds_master
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds_slave0:
    url: jdbc:postgresql://localhost:5432/ds_slave0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds_slave1:
    url: jdbc:postgresql://localhost:5432/ds_slave1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave0
    - ds_slave1
```

### 数据脱敏

```yaml
schemaName: encrypt_db

dataSource:
  url: jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false
  username: root
  password:
  connectionTimeoutMilliseconds: 30000
  idleTimeoutMilliseconds: 60000
  maxLifetimeMilliseconds: 1800000
  maxPoolSize: 50

encryptRule:
  encryptors:
    encryptor_aes:
      type: aes
      props:
        aes.key.value: 123456abc
    encryptor_md5:
      type: md5
  tables:
    t_encrypt:
      columns:
        user_id:
          plainColumn: user_plain
          cipherColumn: user_cipher
          encryptor: encryptor_aes
        order_id:
          cipherColumn: order_cipher
          encryptor: encryptor_md5
```

### 数据分片 + 读写分离

```yaml
schemaName: sharding_master_slave_db

dataSources:
  ds0:
    url: jdbc:postgresql://localhost:5432/ds0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds0_slave0:
    url: jdbc:postgresql://localhost:5432/ds0_slave0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds0_slave1:
    url: jdbc:postgresql://localhost:5432/ds0_slave1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds1:
    url: jdbc:postgresql://localhost:5432/ds1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds1_slave0:
    url: jdbc:postgresql://localhost:5432/ds1_slave0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds1_slave1:
    url: jdbc:postgresql://localhost:5432/ds1_slave1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ms_ds${0..1}.t_order${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ms_ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ms_ds${0..1}.t_order_item${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ms_ds${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_item_id
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultTableStrategy:
    none:
  
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

### 数据分片 + 数据脱敏

dataSources:

```yaml
schemaName: sharding_db

dataSources:
  ds0: 
    url: jdbc:postgresql://localhost:5432/ds0
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
  ds1:
    url: jdbc:postgresql://localhost:5432/ds1
    username: root
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65

shardingRule:
  tables:
    t_order: 
      actualDataNodes: ds${0..1}.t_order${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_item_id
  bindingTables:
    - t_order,t_order_item
  defaultTableStrategy:
    none:
    
  encryptRule:
    encryptors:
      encryptor_aes:
        type: aes
        props:
          aes.key.value: 123456abc
    tables:
      t_order:
        columns:
          order_id:
            plainColumn: order_plain
            cipherColumn: order_cipher
            encryptor: encryptor_aes
```

## 全局配置示例

ShardingSphere-Proxy使用conf/server.yaml配置注册中心、认证信息以及公用属性。

### 治理
治理模块目前支持配置中心和注册中心，具体配置为：
- `orchestrationType: config_center`   #配置配置中心
- `orchestrationType: registry_center` #配置注册中心
- `orchestrationType: config_center,registry_center` #同时配置配置中心和注册中心

```yaml
#省略数据分片和读写分离配置

orchestration:
  orchestration_ds: 
    orchestrationType: config_center,registry_center
    instanceType: zookeeper
    serverLists: localhost:2181
    namespace: orchestration
    props:
      overwrite: true
```

### 认证信息

```yaml
authentication:
  users:
    root:
      password: root
    sharding:
      password: sharding 
      authorizedSchemas: sharding_db
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
  <data_source_name>: #与ShardingSphere-JDBC配置不同，无需配置数据库连接池
    url: #数据库url连接
    username: #数据库用户名
    password: #数据库密码
    connectionTimeoutMilliseconds: 30000 #连接超时毫秒数
    idleTimeoutMilliseconds: 60000 #空闲连接回收超时毫秒数
    maxLifetimeMilliseconds: 1800000 #连接最大存活时间毫秒数
    maxPoolSize: 65 #最大连接数

shardingRule: #省略数据分片配置，与ShardingSphere-JDBC配置一致
```

### 读写分离

```yaml
schemaName: #逻辑数据源名称

dataSources: #省略数据源配置，与数据分片一致

masterSlaveRule: #省略读写分离配置，与ShardingSphere-JDBC配置一致
```

### 数据脱敏
```yaml
dataSource: #省略数据源配置

encryptRule:
  encryptors:
    <encryptor-name>:
      type: #加解密器类型，可自定义或选择内置类型：MD5/AES 
      props: #属性配置, 注意：使用AES加密器，需要配置AES加密器的KEY属性：aes.key.value
        aes.key.value: 
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: #存储明文的字段
          cipherColumn: #存储密文的字段
          assistedQueryColumn: #辅助查询字段，针对ShardingQueryAssistedEncryptor类型的加解密器进行辅助查询
          encryptor: #加密器名字
props:
  query.with.cipher.column: true #是否使用密文列查询
```

## 全局配置项说明

### 治理

与ShardingSphere-JDBC配置一致。

### Proxy属性

```yaml
#省略与ShardingSphere-JDBC一致的配置属性
props:
  acceptor.size: #用于设置接收客户端请求的工作线程个数，默认为CPU核数*2
  proxy.transaction.type: #默认为LOCAL事务，允许LOCAL，XA，BASE三个值，XA采用Atomikos作为事务管理器，BASE类型需要拷贝实现ShardingTransactionManager的接口的jar包至lib目录中
  proxy.opentracing.enabled: #是否开启链路追踪功能，默认为不开启。详情请参见[链路追踪](/cn/features/orchestration/apm/)
  check.table.metadata.enabled: #是否在启动时检查分表元数据一致性，默认值: false
  proxy.frontend.flush.threshold: # 对于单个大查询,每多少个网络包返回一次
```

### 权限验证

用于执行登录Sharding Proxy的权限验证。配置用户名、密码、可访问的数据库后，必须使用正确的用户名、密码才可登录Proxy。

```yaml
authentication:
  users:
    root: # 自定义用户名
      password: root # 自定义用户名
    sharding: # 自定义用户名
      password: sharding # 自定义用户名
      authorizedSchemas: sharding_db, masterslave_db # 该用户授权可访问的数据库，多个用逗号分隔。缺省将拥有root权限，可访问全部数据库。
```

## Yaml语法说明

`!!` 表示实例化该类

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
