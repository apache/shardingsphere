+++
toc = true
title = "编排治理"
weight = 1
+++

2.x版本开始，Sharding-JDBC提供了数据库治理功能，主要包括：

* 配置集中化与动态化，可支持数据源、表与分片及读写分离策略的动态切换
* 数据治理。提供熔断数据库访问程序对数据库的访问和禁用从库的访问的能力
* 支持Zookeeper和Etcd的注册中心

## Zookeeper注册中心

请使用Zookeeper 3.4.6及其以上版本搭建注册中心。[详情参见](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html)

## Etcd注册中心

请使用Etcd V3及其以上版本搭建注册中心。[详情参见](https://coreos.com/etcd/docs/latest)

## 注册中心数据结构

注册中心在定义的命名空间下，创建数据库访问对象运行节点，用于区分不同数据库访问实例。命名空间中包含2个数据子节点，分别是config, state。

## config节点

数据治理相关配置信息，以JSON格式存储，包括数据源，分库分表，读写分离、ConfigMap及Properties配置，可通过修改节点来实现对于配置的动态管理。

```
config
    ├──datasource                                数据源配置
    ├──sharding                                  分库分表（包括分库分表+读写分离）配置根节点
    ├      ├──rule                               分库分表（包括分库分表+读写分离）规则
    ├      ├──configmap                          分库分表ConfigMap配置，以K/V形式存储，如：{"key1":"value1"}
    ├      ├──props                              Properties配置
    ├──masterslave                               读写分离独立使用配置
    ├      ├──rule                               读写分离规则
    ├      ├──configmap                          读写分离ConfigMap配置，以K/V形式存储，如：{"key1":"value1"}
```

### datasource子节点

多个数据库连接池的集合，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid, HikariCP）

```json
[{"name":"demo_ds","clazz":"org.apache.commons.dbcp.BasicDataSource","defaultAutoCommit":"true","defaultReadOnly":"false","defaultTransactionIsolation":"-1","driverClassName":"com.mysql.jdbc.Driver","initialSize":"0","logAbandoned":"false","maxActive":"8","maxIdle":"8","maxOpenPreparedStatements":"-1","maxWait":"-1","minEvictableIdleTimeMillis":"1800000","minIdle":"0","numTestsPerEvictionRun":"3","password":"","removeAbandoned":"false","removeAbandonedTimeout":"300","testOnBorrow":"false","testOnReturn":"false","testWhileIdle":"false","timeBetweenEvictionRunsMillis":"-1","url":"jdbc:mysql://localhost:3306/demo_ds","username":"root","validationQueryTimeout":"-1"}]
```

### sharding子节点

#### rule子节点

分库分表配置，包括分库分表+读写分离配置

```json
{"tableRuleConfigs":[{"logicTable":"t_order","actualDataNodes":"demo_ds.t_order_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_id"},{"logicTable":"t_order_item","actualDataNodes":"demo_ds.t_order_item_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_item_id"}],"bindingTableGroups":["t_order, t_order_item"],"defaultDatabaseShardingStrategyConfig":{},"defaultTableShardingStrategyConfig":{},"masterSlaveRuleConfigs":[]}
```

#### configmap子节点

分库分表ConfigMap配置，以K/V形式存储

```json
{"key1":"value1"}
```

#### props子节点

相对于sharding-jdbc配置里面的Sharding Properties

```json
{"executor.size":"20","sql.show":"true"}
```

### masterslave子节点

#### rule子节点

读写分离独立使用时使用该配置

```json
{"name":"ds_ms","masterDataSourceName":"ds_master","slaveDataSourceNames":["ds_slave_0","ds_slave_1"],"loadBalanceAlgorithmType":"ROUND_ROBIN"}
```

#### configmap子节点

读写分离ConfigMap配置，以K/V形式存储

```json
{"key1":"value1"}
```

## state节点

state节点包括instances和datasource节点。

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....                                    
```

### instances节点
数据库访问对象运行实例信息，子节点是当前运行实例的标识。运行实例标识由运行服务器的IP地址和PID构成。运行实例标识均为临时节点，当实例上线时注册，下线时自动清理。注册中心监控这些节点的变化来治理运行中实例对数据库的访问等。

### datasource节点
可以治理读写分离从库，可动态添加删除以及禁用，预计2.0.0.M3发布
