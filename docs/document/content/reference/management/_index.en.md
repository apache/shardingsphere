+++
pre = "<b>7.1. </b>"
title = "Management"
weight = 1
+++

## Data Structure in Registry Center

Under defined namespace, `rules`, `props` and `metadata` nodes persist in YAML, modifying nodes can dynamically refresh configurations. `nodes` node persist the runtime node of database access object, to distinguish different database access instances.

```
namespace
   ├──rules                                   # Global rule configuration
   ├──props                                   # Properties configuration
   ├──metadata                                # Metadata configuration
   ├     ├──${schema_1}                       # Schema name 1
   ├     ├     ├──dataSources                 # Datasource configuration
   ├     ├     ├──rules                       # Rule configuration
   ├     ├     ├──tables                      # Table configuration
   ├     ├     ├     ├──t_1 
   ├     ├     ├     ├──t_2      
   ├     ├──${schema_2}                       # Schema name 2
   ├     ├     ├──dataSources                 # Datasource configuration
   ├     ├     ├──rules                       # Rule configuration
   ├     ├     ├──tables                      # Table configuration
   ├──nodes
   ├    ├──compute_nodes
   ├    ├     ├──online
   ├    ├     ├     ├──proxy
   ├    ├     ├     ├     ├──${your_instance_ip_a}@${your_instance_port_x}
   ├    ├     ├     ├     ├──${your_instance_ip_b}@${your_instance_port_y}
   ├    ├     ├     ├     ├──....
   ├    ├     ├     ├──jdbc
   ├    ├     ├     ├     ├──${your_instance_ip_a}@${your_instance_pid_x}
   ├    ├     ├     ├     ├──${your_instance_ip_b}@${your_instance_pid_y}
   ├    ├     ├     ├     ├──....   
   ├    ├     ├──attributies
   ├    ├     ├     ├──${your_instance_ip_a}@${your_instance_port_x}
   ├    ├     ├     ├     ├──status
   ├    ├     ├     ├     ├──label    
   ├    ├     ├     ├──${your_instance_ip_b}@${your_instance_pid_y}
   ├    ├     ├     ├     ├──status   
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

Properties configuration. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-jdbc/props/) for more details.

```yaml
kernel-executor-size: 20
sql-show: true
```

### /metadata/${schemaName}/dataSources

A collection of multiple database connection pools, whose properties (e.g. DBCP, C3P0, Druid and HikariCP) are configured by users themselves.

```yaml
ds_0:
  initializationFailTimeout: 1
  validationTimeout: 5000
  maxLifetime: 1800000
  leakDetectionThreshold: 0
  minimumIdle: 1
  password: root
  idleTimeout: 60000
  jdbcUrl: jdbc:mysql://127.0.0.1:3306/ds_0?serverTimezone=UTC&useSSL=false
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  maximumPoolSize: 50
  connectionTimeout: 30000
  username: root
  poolName: HikariPool-1
ds_1:
  initializationFailTimeout: 1
  validationTimeout: 5000
  maxLifetime: 1800000
  leakDetectionThreshold: 0
  minimumIdle: 1
  password: root
  idleTimeout: 60000
  jdbcUrl: jdbc:mysql://127.0.0.1:3306/ds_1?serverTimezone=UTC&useSSL=false
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  maximumPoolSize: 50
  connectionTimeout: 30000
  username: root
  poolName: HikariPool-2
```

### /metadata/${schemaName}/rules

Rule configurations, including sharding, readwrite-splitting, data encryption, shadow DB configurations.

```yaml
- !SHARDING
  xxx
  
- !READWRITE_SPLITTING
  xxx
  
- !ENCRYPT
  xxx
```

### /metadata/${schemaName}/tables

Use separate node storage for each table, dynamic modification of metadata content is not supported currently.

```yaml
name: t_order                             # Table name
columns:                                  # Columns
  id:                                     # Column name
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
indexs:                                   # Index
  t_user_order_id_index:                  # Index name
    name: t_user_order_id_index
```

### /nodes/compute_nodes

It includes running instance information of database access object, with sub-nodes as the identifiers of currently running instance, which consist of IP and PORT. Those identifiers are temporary nodes, which are registered when instances are on-line and cleared when instances are off-line. The registry center monitors the change of those nodes to govern the database access of running instances and other things.

### /nodes/storage_nodes

It is able to orchestrate replica database, delete or disable data dynamically.
