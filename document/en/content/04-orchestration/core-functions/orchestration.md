+++
toc = true
title = "Orchestration"
weight = 1
+++

Sharding-JDBC provides orchestration for databases in Version 2.x, which mainly includes:

* The centralized and dynamic configuration can support the dynamic strategy switching of Sharding and read-write splitting.
* Provide the circuit-breaker mechanism for database access, and the switch that disables access to Slaves.
* Support for Zookeeper and Etcd registry.

# 

## Zookeeper

Please use Zookeeper 3.4.6 and above to set up the registration center. [Reference](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html)

## Etcd

Please use Etcd V3 and above to set up the registration center. [Reference](https://coreos.com/etcd/docs/latest)

## The structure of registry

The registry is defined in the namespace and you can create the object running node to access the database, by which you can distinguish different accessing instances. The namespace contains two child nodes, namely, config and state.

## The config node

It mainly includes the data-management related configuration information such as data source, Sharding, Read-write splitting, ConfigMap and configuration of the Properties, stored in a JSON format. You can modify this node to get dynamic configuration management.

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

### The child node of data source

It is a collection of multiple database connection pools, and the properties of different connection pools should be configured by users, e.g. DBCP，C3P0，Druid, HikariCP.

```json
[{"name":"demo_ds","clazz":"org.apache.commons.dbcp.BasicDataSource","defaultAutoCommit":"true","defaultReadOnly":"false","defaultTransactionIsolation":"-1","driverClassName":"com.mysql.jdbc.Driver","initialSize":"0","logAbandoned":"false","maxActive":"8","maxIdle":"8","maxOpenPreparedStatements":"-1","maxWait":"-1","minEvictableIdleTimeMillis":"1800000","minIdle":"0","numTestsPerEvictionRun":"3","password":"","removeAbandoned":"false","removeAbandonedTimeout":"300","testOnBorrow":"false","testOnReturn":"false","testWhileIdle":"false","timeBetweenEvictionRunsMillis":"-1","url":"jdbc:mysql://localhost:3306/demo_ds","username":"root","validationQueryTimeout":"-1"}]
```

### The child node of sharding

#### The child node of rule

The configuration of Sharding, including the configs of  Sharding and Read-write splitting.

```json
{"tableRuleConfigs":[{"logicTable":"t_order","actualDataNodes":"demo_ds.t_order_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_id"},{"logicTable":"t_order_item","actualDataNodes":"demo_ds.t_order_item_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_item_id"}],"bindingTableGroups":["t_order, t_order_item"],"defaultDatabaseShardingStrategyConfig":{},"defaultTableShardingStrategyConfig":{},"masterSlaveRuleConfigs":[]}
```

#### The child node of ConfigMap

The ConfigMap config of Sharding, stored in the form of K/V.

```json
{"key1":"value1"}
```

#### The child node of props

They are the Sharding Properties in sharding-jdbc configuration.

```json
{"executor.size":"20","sql.show":"true"}
```

### The child node of Master-Slave

#### The child node of rule

The configuration for using Read-write splitting alone.

```json
{"name":"ds_ms","masterDataSourceName":"ds_master","slaveDataSourceNames":["ds_slave_0","ds_slave_1"],"loadBalanceAlgorithmType":"ROUND_ROBIN"}
```

#### The child of ConfigMap

The ConfigMap config of Sharding, stored in the form of K/V.

```json
{"key1":"value1"}
```

## The state node

It contains the nodes of instance and data source.

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....                                    
```

### The instance node 

It includes the running-instance information of database-accessing object, and its child node is the identity of the current running instance. This identify is composed of IP and PID in the running server and always a temporary node. It is registered when the instance is online, and automatically cleaned when the instance is offline. The registry manages the access to the database by monitoring changes in these nodes.

### The data source node

It is used to manage Read-write splitting and dynamically add, remove or disable data sources (Expected in 2.0.0.M3 release).

