+++
pre = "<b>4.2.2. </b>"
title = "Configuration Manual"
weight = 2
+++

## Data Source and Sharding Configuration Instance

ShardingSphere-Proxy supports multiple logic data source, each one of which is a `yalm` configuration document named with `config-` prefix. The following is the configuration instance of `config-xxx.yaml`.

### Data Sharding

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
      keyGenerateStrategy:
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
      keyGenerateStrategy:
        type: SNOWFLAKE
        column: order_item_id
  bindingTables:
    - t_order,t_order_item
  defaultTableStrategy:
    none:
```

### Read-Write Split

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

### data encryption

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
    aes_encryptor:
      type: AES
      props:
        aes.key.value: 123456abc
    md5_encryptor:
      type: MD5
  tables:
    t_encrypt:
      columns:
        user_id:
          plainColumn: user_plain
          cipherColumn: user_cipher
          encryptorName: aes_encryptor
        order_id:
          cipherColumn: order_cipher
          encryptorName: md5_encryptor
```

### Data Sharding + Read-Write Split

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
      keyGenerateStrategy:
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
      keyGenerateStrategy:
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

### Data Sharding + data encryption

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
      keyGenerateStrategy:
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
      keyGenerateStrategy:
        type: SNOWFLAKE
        column: order_item_id
  bindingTables:
    - t_order,t_order_item
  defaultTableStrategy:
    none:
    
  encryptRule:
    encryptors:
      aes_encryptor:
        type: AES
        props:
          aes.key.value: 123456abc
    tables:
      t_order:
        columns:
          order_id:
            plainColumn: order_plain
            cipherColumn: order_cipher
            encryptorName: aes_encryptor 
```

## Overall Configuration Instance

ShardingSphere-Proxy uses `conf/server.yaml` to configure the registry center, authentication information and common properties.

### Orchestration
Orchestration can config config-center and registry-center now, as follow:
- `orchestrationType: config_center`   #config config-center
- `orchestrationType: registry_center` #config registry_center
- `orchestrationType: config_center,registry_center` #config config-center and registry_center

```yaml
#Omit data sharding and read-write split configurations

orchestration:
  orchestration_ds: 
    orchestrationType: config_center,registry_center
    instanceType: zookeeper
    serverLists: localhost:2181
    namespace: orchestration
    properties:
      overwrite: true
```

### Authentication

```yaml
authentication:
  users:
    root:
      password: root
    sharding:
      password: sharding 
      authorizedSchemas: sharding_db
```

### Common property

```yaml
properties:
  executor.size: 16
  sql.show: false
```

## Data Source and Sharding Configuration Item Explanation

### Data Sharding

```yaml
schemaName: #Logic data schema name

dataSources: #Data source configuration, which can be multiple data_source_name
  <data_source_name>: #Different from ShardingSphere-JDBC configuration, it does not need to be configured with database connection pool
    url: #Database url connection
    username: #Database username
    password: #Database password
    connectionTimeoutMilliseconds: 30000 #Connection timeout
    idleTimeoutMilliseconds: 60000 #Idle timeout setting
    maxLifetimeMilliseconds: 1800000 #Maximum lifetime
    maxPoolSize: 65 #Maximum connection number in the pool

shardingRule: #Omit data sharding configuration and be consistent with ShardingSphere-JDBC configuration
```

### Read-Write Split

```yaml
schemaName: #Logic data schema name

dataSources: #Omit data source configurations; keep it consistent with data sharding

masterSlaveRule: #Omit data source configurations; keep it consistent with ShardingSphere-JDBC
```

### data encryption
```yaml
dataSource: #Ignore data sources configuration

encryptRule:
  encryptors:
    <encrypt-algorithm-name>:
      type: #encrypt algorithm type
      props: #Properties, e.g. `aes.key.value` for AES encrypt algorithm
        aes.key.value: 
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: #plaintext column name
          cipherColumn: #ciphertext column name
          assistedQueryColumn: #AssistedColumns for query，when use QueryAssistedEncryptAlgorithm, it can help query encrypted data
          encryptorName: #encrypt algorithm name
properties:
  query.with.cipher.column: true #Whether use cipherColumn to query or not
```

## Overall Configuration Explanation

### Orchestration

It is the same with ShardingSphere-JDBC configuration.

### Proxy Property

```yaml
#Omit configurations that are the same with ShardingSphere-JDBC
properties:
  acceptor.size: #The thread number of accept connection; default to be 2 times of cpu core
  proxy.transaction.type: #Support LOCAL, XA, BASE; Default is LOCAL transaction, for BASE type you should copy ShardingTransactionManager associated jar to lib directory
  proxy.opentracing.enabled: #Whether to enable opentracing, default not to enable; refer to [APM](/en/features/orchestration/apm/) for more details
  check.table.metadata.enabled: #Whether to check metadata consistency of sharding table when it initializes; default value: false
```

### Authentication

It is used to verify the authentication to log in ShardingSphere-Proxy, which must use correct user name and password after the configuration of them.

```yaml
authentication:
  users:
    root: # self-defined username
      password: root # self-defined password
    sharding: # self-defined username
      password: sharding # self-defined password
      authorizedSchemas: sharding_db, masterslave_db # schemas authorized to this user, please use commas to connect multiple schemas. Default authorizedSchemas is all of the schemas.
```

## Yaml Syntax Explanation

`!!` means instantiation of that class

`-` means one or multiple can be included

`[]` means array, substitutable with `-`