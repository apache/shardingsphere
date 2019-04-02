+++
pre = "<b>4.2.2. </b>"
toc = true
title = "Configuration Manual"
weight = 2

+++

## Data Source and Sharding Configuration Instance

Sharding-Proxy supports multiple logic data source, each one of which is a `yalm` configuration document named with `config-` prefix. The following is the configuration instance of `config-xxx.yaml`.

### Data Sharding

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
        column: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
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
      algorithmExpression: ds${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
```

### Read-Write Split

```yaml
schemaName: sharding_master_slave_db

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

### Data Sharding + Read-Write Split

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

## Overall Configuration Instance

Sharding-Proxy uses `conf/server.yaml` to configure the registry center, authentication information and common properties.

### Data Orchestration

```yaml
#Omit data sharding and read-write split configurations

orchestration:
  name: orchestration_ds
  overwrite: true
  registry:
    namespace: orchestration
    serverLists: localhost:2181
```

### Authentication

```yaml
authentication:
  username: root
  password:
```

### Common property

```yaml
props:
  executor.size: 16
  sql.show: false
```

## Data Source and Sharding Configuration Item Explanation

### Data Sharding

```yaml
schemaName: #Logic data source name

dataSources: #Data source configuration, which can be multiple data_source_name
  <data_source_name>: #Different from Sharding-JDBC configuration, it does not need to be configured with database connection pool
    url: #Database url connection
    username: #Database username
    password: #Database password
    connectionTimeoutMilliseconds: 30000 #Connection timeout
    idleTimeoutMilliseconds: 60000 #Idle timeout setting
    maxLifetimeMilliseconds: 1800000 #Maximum lifetime
    maxPoolSize: 65 #Maximum connection number in the pool
    minPoolSize: 1 #Minimum connection number in the pool
    maintenanceIntervalMilliseconds: 30000 #Interval time for connection maintenance 

shardingRule: #Omit data sharding configuration and be consistent with Sharding-JDBC configuration
```

### Read-Write Split

```yaml
schemaName: #Logic data source name

dataSources: #Omit data source configurations; keep it consistent with data sharding

masterSlaveRule: #Omit data source configurations; keep it consistent with Sharding-JDBC
```

## Overall Configuration Explanation

### Data Orchestration

It is the same with Sharding-JDBC configuration.

### Proxy Property

```yaml
#Omit configurations that are the same with Sharding-JDBC
props:
  acceptor.size: #The thread number of accept connection; default to be 2 times of cpu core
  proxy.transaction.enabled: #Whether to enable transaction; it only supports XA transactions, default not to enable
  proxy.opentracing.enabled: #Whether to enable opentracing, default not to enable; refer to [APM](/en/features/orchestration/apm/) for more details
  check.table.metadata.enabled: #Whether to check metadata consistency of sharding table when it initializes; default value: false
```

### Authentication

It is used to verify the authentication to log in Sharding-Proxy, which must use correct user name and password after the configuration of them.

```yaml
authentication:
   username: root
   password:
```

## Yaml Syntax Explanation

`!!` means instantiation of that class

`-` means one or multiple can be included

`[]` means array, substitutable with `-`