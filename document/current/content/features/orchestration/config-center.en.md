+++
pre = "<b>3.3.1. </b>"
toc = true
title = "Config Center"
weight = 1
+++

## Motivation

- Configuration centralization: 
increasing runtime instances make it hard to manage separate configurations and asynchronized configurations can cause serious problems. 
Concentrating configurations in configuration center makes it more effective to manage.

- Dynamic configuration: 
distribution after configuration modification is another important capability that configuration center can provide. 
It can support dynamic switch between data sources, tables, shards and the read-write split strategy.

## Data Structure in Configuration Center

Under configuration of defined name space, configuration center stores data source, sharding databases, sharding tables, read-write split, and Properties configurations in YAML form. 
Modifying nodes can dynamically manage configuration.

```
config
    ├──authentication                            # Sharding-Proxy authentication configuration
    ├──props                                     # Properties configuration
    ├──schema                                    # Schema configuration
    ├      ├──sharding_db                        # SchemaName configuration
    ├      ├      ├──datasource                  # Datasource configuration
    ├      ├      ├──rule                        # Sharding rule configuration
    ├      ├──masterslave_db                     # SchemaName configuration
    ├      ├      ├──datasource                  # Datasource configuration
    ├      ├      ├──rule                        # Master-slave rule configuration
```

### config/authentication

```yaml
password: root
username: root
```

### config/sharding/props

Correspond to Sharding Properties in ShardingSphere configuration.

```yaml
executor.size: 20
sql.show: true
```

### config/schema/schemeName/datasource

A collection of multiple database connection pools, whose properties (e.g. DBCP，C3P0，Druid, HikariCP) are configured by users themselves.

```yaml
ds_0: !!org.apache.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  properties:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    maintenanceIntervalMilliseconds: 30000
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
ds_1: !!org.apache.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  properties:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    maintenanceIntervalMilliseconds: 30000
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
```

### config/schema/sharding_db/rule

Database and table sharding configuration, including sharding + read-write split configuration.

```yaml
tables:
  t_order:
    actualDataNodes: ds_$->{0..1}.t_order_$->{0..1}
    keyGenerator:
      column: order_id
    logicTable: t_order
    tableStrategy:
      inline:
        algorithmExpression: t_order_$->{order_id % 2}
        shardingColumn: order_id
  t_order_item:
    actualDataNodes: ds_$->{0..1}.t_order_item_$->{0..1}
    keyGenerator:
      column: order_item_id
    logicTable: t_order_item
    tableStrategy:
      inline:
        algorithmExpression: t_order_item_$->{order_id % 2}
        shardingColumn: order_id
bindingTables:
  - t_order,t_order_item
broadcastTables:
  - t_config
  
defaultDataSourceName: ds_0
defaultDatabaseStrategy:
  inline:
    algorithmExpression: ds_$->{user_id % 2}
    shardingColumn: user_id
    
masterSlaveRules: {}
```

### config/schema/masterslave/rule

This configuration is used when read-write split is used alone.

```yaml
name: ds_ms
masterDataSourceName: ds_master 
slaveDataSourceNames:
  - ds_slave0
  - ds_slave1
loadBalanceAlgorithmType: ROUND_ROBIN
```

## Dynamic Effectiveness

Modification, deletion and addition of relevant configurations in registry center will be pushed to the production environment and take effect immediately.
