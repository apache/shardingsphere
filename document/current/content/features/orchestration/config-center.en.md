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

Config center is defined in the namespace under `config` node. It mainly includes the data-management related configuration information such as data source, Sharding, Read-write splitting, ConfigMap and configuration of the Properties, stored in a JSON format. You can modify this node to get dynamic configuration management.

```
config
    ├──datasource                                # The config of data source 
    ├──sharding                                  # The root node of Sharding configuration
    ├      ├──rule                               # The rule of Sharding
    ├      ├──configmap                          # The ConfigMap config of Sharding, stored in the form of K/V, e.g. {"key1":"value1"}
    ├      ├──props                              # The config of Properties
    ├──masterslave                               # The config of Read-write splitting
    ├      ├──rule                               # The rule of Read-write splitting 
    ├      ├──configmap                          # The ConfigMap config of Read-write splitting, stored in the form of K/V, e.g. {"key1":"value1"}
```

### config/datasource

It is a collection of multiple database connection pools, and the properties of different connection pools should be configured by users, e.g. DBCP，C3P0，Druid, HikariCP.

```json
[
    {
        "name": "demo_ds", 
        "clazz": "org.apache.commons.dbcp.BasicDataSource", 
        "defaultAutoCommit": "true", 
        "defaultReadOnly": "false", 
        "defaultTransactionIsolation": "-1", 
        "driverClassName": "com.mysql.jdbc.Driver", 
        "initialSize": "0", 
        "logAbandoned": "false", 
        "maxActive": "8", 
        "maxIdle": "8", 
        "maxOpenPreparedStatements": "-1", 
        "maxWait": "-1", 
        "minEvictableIdleTimeMillis": "1800000", 
        "minIdle": "0", 
        "numTestsPerEvictionRun": "3", 
        "password": "", 
        "removeAbandoned": "false", 
        "removeAbandonedTimeout": "300", 
        "testOnBorrow": "false", 
        "testOnReturn": "false", 
        "testWhileIdle": "false", 
        "timeBetweenEvictionRunsMillis": "-1", 
        "url": "jdbc:mysql://localhost:3306/demo_ds", 
        "username": "root", 
        "validationQueryTimeout": "-1"
    }
]
```

### config/sharding/rule

The configuration of Sharding, including the configs of  Sharding and Read-write splitting.

```json
{
    "tableRuleConfigs": [
        {
            "logicTable": "t_order", 
            "actualDataNodes": "demo_ds.t_order${0..1}", 
            "databaseShardingStrategyConfig": { }, 
            "tableShardingStrategyConfig": {
                "type": "STANDARD", 
                "shardingColumn": "order_id", 
                "preciseAlgorithmClassName": "io.shardingsphere.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm", 
                "rangeAlgorithmClassName": ""
            }, 
            "keyGeneratorColumnName": "order_id"
        }, 
        {
            "logicTable": "t_order_item", 
            "actualDataNodes": "demo_ds.t_order_item${0..1}", 
            "databaseShardingStrategyConfig": { }, 
            "tableShardingStrategyConfig": {
                "type": "STANDARD", 
                "shardingColumn": "order_id", 
                "preciseAlgorithmClassName": "io.shardingsphere.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm", 
                "rangeAlgorithmClassName": ""
            }, 
            "keyGeneratorColumnName": "order_item_id"
        }
    ], 
    "bindingTableGroups": [
        "t_order, t_order_item"
    ], 
    "defaultDatabaseShardingStrategyConfig": { }, 
    "defaultTableShardingStrategyConfig": { }, 
    "masterSlaveRuleConfigs": [ ]
}
```

### config/sharding/configmap

Config map of Sharding, stored in the form of K/V.

```json
{
    "key1": "value1"
}
```

### config/sharding/props

They are the Sharding Properties in sharding-sphere configuration.

```json
{
    "executor.size": "20", 
    "sql.show": "true"
}
```

### config/masterslave/rule

The configuration for using Read-write splitting standalone.

```json
{
    "name": "ds_ms", 
    "masterDataSourceName": "ds_master", 
    "slaveDataSourceNames": [
        "ds_slave0", 
        "ds_slave1"
    ], 
    "loadBalanceAlgorithmType": "ROUND_ROBIN"
}
```

### config/masterslave/configmap

Config map of read-write splitting, stored in the form of K/V.

```json
{
    "key1": "value1"
}
```
