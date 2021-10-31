+++
title = "Registry Center"
weight = 1
+++

## Motivation

- Centralized configuration: more and more running examples have made it hard to manage separate configurations and asynchronized configurations can cause serious problems. Concentrating them in the configuration center can make the management more effective.

- Dynamic configuration: distribution after configuration modification is another important capability of configuration center. It can support dynamic switch between data sources and rule configurations.

- Hold all ephemeral status data dynamically generated in runtime(such as available proxy instances, disabled datasource instances etc).

- Disable the access to replica database and the access of application. Governance still has many functions(such as flow control) to be developed.

## Data Structure in Registry Center

Under defined namespace, `rules`, `props` and `metadata` nodes persist in YAML, modifying nodes can dynamically refresh configurations. `status` node persist the runtime node of database access object, to distinguish different database access instances.

```
namespace
   ├──rules                                     # Global rule configuration
   ├──props                                     # Properties configuration
   ├──metadata                                  # Metadata configuration
   ├      ├──${schema_1}                        # Schema name 1
   ├      ├      ├──dataSources                 # Datasource configuration
   ├      ├      ├──rules                       # Rule configuration
   ├      ├      ├──schema                      # Table configuration
   ├      ├──${schema_2}                        # Schema name 2
   ├      ├      ├──dataSources                 # Datasource configuration
   ├      ├      ├──rules                       # Rule configuration
   ├      ├      ├──schema                      # Table configuration
   ├──status
   ├    ├──compute_nodes
   ├    ├     ├──online
   ├    ├     ├     ├──${your_instance_ip_a}@${your_instance_port_x}
   ├    ├     ├     ├──${your_instance_ip_b}@${your_instance_port_y}
   ├    ├     ├     ├──....
   ├    ├     ├──circuit_breaker
   ├    ├     ├     ├──${your_instance_ip_c}@${your_instance_port_v}
   ├    ├     ├     ├──${your_instance_ip_d}@${your_instance_port_w}
   ├    ├     ├     ├──....
   ├    ├──storage_nodes
   ├    ├     ├──disable
   ├    ├     ├      ├──${schema_1.ds_0}
   ├    ├     ├      ├──${schema_1.ds_1}
   ├    ├     ├      ├──....
   ├    ├     ├──primary
   ├    ├     ├      ├──${schema_2.ds_0}
   ├    ├     ├      ├──${schema_2.ds_1}
   ├    ├     ├      ├──....
```

### /rules

global rule configurations， including configure the username and password for ShardingSphere-Proxy.

```yaml
- !AUTHORITY
users:
  - root@%:root
  - sharding@127.0.0.1:sharding
provider:
  type: ALL_PRIVILEGES_PERMITTED
```

### /props

Properties configuration. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-jdbc/configuration/) for more details.

```yaml
kernel-executor-size: 20
sql-show: true
```

### /metadata/${schemeName}/dataSources

A collection of multiple database connection pools, whose properties (e.g. DBCP, C3P0, Druid and HikariCP) are configured by users themselves.

```yaml
ds_0: 
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  props:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
ds_1: 
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  props:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
```

### /metadata/${schemeName}/rules

Rule configurations, including sharding, readwrite-splitting, data encryption, shadow DB configurations.

```yaml
- !SHARDING
  xxx
  
- !READWRITE_SPLITTING
  xxx
  
- !ENCRYPT
  xxx
```

### /metadata/${schemeName}/schema

Dynamic modification of metadata content is not supported currently.

```yaml
tables:                                       # Tables
  t_order:                                    # table_name
    columns:                                  # Columns
      id:                                     # column_name
        caseSensitive: false
        dataType: 0
        generated: false
        name: id
        primaryKey: trues
      order_id:
        caseSensitive: false
        dataType: 0
        generated: false
        name: order_id
        primaryKey: false
    indexs:                                   # Indexes
      t_user_order_id_index:                  # index_name
        name: t_user_order_id_index
  t_order_item:
    columns:
      order_id:
        caseSensitive: false
        dataType: 0
        generated: false
        name: order_id
        primaryKey: false
```

### /status/compute_nodes

It includes running instance information of database access object, with sub-nodes as the identifiers of currently running instance, which consist of IP and PORT. Those identifiers are temporary nodes, which are registered when instances are on-line and cleared when instances are off-line. The registry center monitors the change of those nodes to govern the database access of running instances and other things.

### /status/storage_nodes

It is able to orchestrate replica database, delete or disable data dynamically.

## Dynamic Effectiveness

Modification, deletion and insertion of relevant configurations in the config center will immediately take effect in the producing environment.

## Operation Guide

### Circuit Breaker

Write `DISABLED` (case insensitive) to `IP@PORT` to disable that instance; delete `DISABLED` to enable the instance.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/status/compute_nodes/circuit_breaker/${your_instance_ip_a}@${your_instance_port_x} DISABLED
```

### Disable Replica Database

Under readwrite-splitting scenarios, users can write `DISABLED` (case insensitive) to sub-nodes of data source name to disable replica database sources. Delete `DISABLED` or the node to enable it.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/status/storage_nodes/disable/${your_schema_name.your_replica_datasource_name} DISABLED
```
