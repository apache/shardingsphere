+++
pre = "<b>3.3.1. </b>"
title = "Config Center"
weight = 1
+++

## Motivation

- Centralized configuration: more and more running examples have made it hard to manage separate configurations and asynchronized configurations can cause serious problems. Concentrating them in the configuration center can make the management more effective.

- Dynamic configuration: distribution after configuration modification is another important capability of configuration center. It can support dynamic switch between data sources, tables, shards and the read-write split strategy.

## Data Structure in Configuration Center

Under defined name space config, configuration center stores data sources, sharding databases, sharding tables, read-write split, and properties in YAML. Modifying nodes can dynamically manage configuration.

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

It corresponds to Sharding Properties in ShardingSphere configuration.

```yaml
executor.size: 20
sql.show: true
```

### config/schema/schemeName/datasource

A collection of multiple database connection pools, whose properties (e.g. DBCP, C3P0, Druid and HikariCP) are configured by users themselves.

```yaml
ds_0: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration
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
ds_1: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration
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

It refers to sharding configuration, including sharding + read-write split configuration.

```yaml
tables:
  t_order:
    actualDataNodes: ds_$->{0..1}.t_order_$->{0..1}
    databaseStrategy:
      inline:
        shardingColumn: user_id
        algorithmExpression: ds_$->{user_id % 2}
    keyGenerator:
      column: order_id
    logicTable: t_order
    tableStrategy:
      inline:
        shardingColumn: order_id
        algorithmExpression: t_order_$->{order_id % 2}
  t_order_item:
    actualDataNodes: ds_$->{0..1}.t_order_item_$->{0..1}
    databaseStrategy:
      inline:
        shardingColumn: user_id
        algorithmExpression: ds_$->{user_id % 2}
    keyGenerator:
      column: order_item_id
    logicTable: t_order_item
    tableStrategy:
      inline:
        shardingColumn: order_id
        algorithmExpression: t_order_item_$->{order_id % 2}
bindingTables:
  - t_order,t_order_item
broadcastTables:
  - t_config

defaultDataSourceName: ds_0

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

Modification, deletion and insertion of relevant configurations in the registry center will  immediately take effect in the producing environment.
