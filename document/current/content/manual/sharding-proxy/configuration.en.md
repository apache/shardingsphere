+++
pre = "<b>4.2.2. </b>"
toc = true
title = "Configuration Manual"
weight = 2
+++

## Data sources and sharding rule configuration example

Sharding-Proxy support multiple logic schema, for every configuration file which prefix as `config-`, and suffix as `.yaml`. 
Below is configuration example of `config-xxx.yaml`.

### Sharding

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

### Read-write splitting

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

### Sharding + Read-write splitting

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

## Global configuration example

### Orchestration

```yaml
#Ignore data sources, sharding and read-write splitting configuration

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

### Common properties

```yaml
props:
  executor.size: 16
  sql.show: false
```

## Data sources and sharding rule configuration reference

### Sharding

```yaml
schemaName: #Logic database schema name

dataSources: #Data sources configuration, multiple `data_source_name` available
  <data_source_name>: #Different with Sharding-JDBC, do not need configure data source pool here.
    url: #Database URL
    username: #Database username
    password: #Database password
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 65
    minPoolSize: 1
    maintenanceIntervalMilliseconds: 30000

shardingRule: #Ignore sharding rule configuration, same as Sharding-JDBC
```

### Read-write splitting

```yaml
schemaName: #Logic database schema name

dataSources: #Ignore data source configuration, same as sharding

masterSlaveRule: #Ignore read-write splitting rule configuration, same as Sharding-JDBC
```

## Global configuration reference

### Orchestration

Same as configuration of Sharding-JDBC.

### Proxy Properties

```yaml
#Ignore configuration which same as Sharding-JDBC
props:
  acceptor.size: #Max thread count to handle client's requests, default value is CPU*2
  proxy.transaction.enabled: #Enable transaction, only support XA now, default value is false
  proxy.opentracing.enabled: #Enable open tracing, default value is false. More details please reference[APM](/en/features/orchestration/apm/)
  check.table.metadata.enabled: #To check the metadata consistency of all the tables or not, default value : false
```

### Authorization for Proxy

To perform Authorization for Sharding Proxy when login in. After configuring the username and password, you must use the correct username and password to login into the Proxy.

```yaml
authentication:
   username: root
   password:
```

## Yaml syntax

`!!` means class instantiation

`-` means one or multiple available

`[]` means array, can replace `-` each other
