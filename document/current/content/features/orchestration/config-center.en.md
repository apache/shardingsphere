+++
pre = "<b>3.3.1. </b>"
toc = true
title = "Config Center"
weight = 1
+++

## Motivation

- Centralize: More and more runtime instances make configuration management complicated. Configuration inconstant is a fatal problem on distribute system. Centralize configuration on config center make management effective.

- Dynamic: Distribute configuration after update is anther important abilities. It supports data source, tables and sharding strategies switch dynamically.

## Data structure

Config center is defined in the namespace under `config` node. It mainly includes the data-management related configuration information such as data source, Sharding, Read-write splitting, ConfigMap and configuration of the Properties, stored in a YAML format. You can modify this node to get dynamic configuration management.

```
config
    ├──authentication                            # Authentation configuration of Sharding-Proxy
    ├──configMap                                 # Config map stored in the form of K/V.
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

### config/configmap

Config map stored in the form of K/V.

```yaml
key2: value2
```

### config/sharding/props

They are the Sharding Properties in sharding-sphere configuration.

```yaml
executor.size: 20
sql.show: true
```

### config/schema/schemeName/datasource

It is a collection of multiple database connection pools, and the properties of different connection pools should be configured by users, e.g. DBCP，C3P0，Druid, HikariCP.

```yaml
ds_0: !!io.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration
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
ds_1: !!io.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration
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

The configuration of Sharding, including the configs of  Sharding and Read-write splitting.

```yaml
tables:
  t_order:
    actualDataNodes: ds_$->{0..1}.t_order_$->{0..1}
    keyGeneratorColumnName: order_id
    logicTable: t_order
    tableStrategy:
      inline:
        algorithmExpression: t_order_$->{order_id % 2}
        shardingColumn: order_id
  t_order_item:
    actualDataNodes: ds_$->{0..1}.t_order_item_$->{0..1}
    keyGeneratorColumnName: order_item_id
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

The configuration for using Read-write splitting standalone.

```yaml
name: ds_ms
masterDataSourceName: ds_master 
slaveDataSourceNames:
  - ds_slave0
  - ds_slave1
loadBalanceAlgorithmType: ROUND_ROBIN
```

## Dynamically push to online

All of changes made in registry will be dynamically pushed to online and take effect immediately.
