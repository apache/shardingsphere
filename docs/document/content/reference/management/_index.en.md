+++
pre = "<b>7.3. </b>"
title = "Management"
weight = 3
+++

## Data Structure in Registry Center

Under defined namespace, `rules`, `props` and `metadata` nodes persist in YAML. Modifying nodes can dynamically refresh configurations.
`nodes` persist the runtime node of database access object, to distinguish different database access instances.
`statistics` persist data records in system tables.

```
namespace
   ├──rules                                              # Global rule configuration
   ├     ├──transaction
   ├     ├     ├──active_version                                     
   ├     ├     ├──versions  
   ├     ├     ├     ├──0       
   ├──props                                              # Properties configuration
   ├     ├──active_verison                                     
   ├     ├──versions  
   ├     ├     ├──0                  
   ├──metadata                                           # Metadata configuration
   ├     ├──${databaseName} 
   ├     ├     ├──data_sources                           # Storage unit configuration
   ├     ├     ├     ├──${dataSourceName}                        
   ├     ├     ├     ├     ├──active_verison             # Active version                                 
   ├     ├     ├     ├     ├──versions                   # version list
   ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├──...                                
   ├     ├     ├──schemas                                # Schema list
   ├     ├     ├     ├──${schemaName}                    
   ├     ├     ├     ├     ├──tables                     # Table configuration
   ├     ├     ├     ├     ├     ├──${tableName}         
   ├     ├     ├     ├     ├     ├     ├──active_verison # Active version                                 
   ├     ├     ├     ├     ├     ├     ├──versions       # version list
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...  
   ├     ├     ├     ├     ├──views                      # View configuration
   ├     ├     ├     ├     ├     ├──${viewName}
   ├     ├     ├     ├     ├     ├     ├──active_verison # Active version                           
   ├     ├     ├     ├     ├     ├     ├──versions       # version list
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...  
   ├     ├     ├──rules
   ├     ├     ├     ├──sharding
   ├     ├     ├     ├     ├──algorithms
   ├     ├     ├     ├     ├     ├──${algorithmName}     # algorithm name
   ├     ├     ├     ├     ├     ├     ├──active_verison # Active version                           
   ├     ├     ├     ├     ├     ├     ├──versions       # version list
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...
   ├     ├     ├     ├     ├──key_generators
   ├     ├     ├     ├     ├     ├──${keyGeneratorName}  # keyGenerator name
   ├     ├     ├     ├     ├     ├     ├──active_verison # Active version                           
   ├     ├     ├     ├     ├     ├     ├──versions       # version list
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...         
   ├     ├     ├     ├     ├──tables
   ├     ├     ├     ├     ├     ├──${tableName}         # logic table name
   ├     ├     ├     ├     ├     ├     ├──active_verison # Active version                           
   ├     ├     ├     ├     ├     ├     ├──versions       # version list
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...          
   ├──nodes
   ├    ├──compute_nodes
   ├    ├     ├──online
   ├    ├     ├     ├──proxy
   ├    ├     ├     ├     ├──UUID             # Proxy instance identifier
   ├    ├     ├     ├     ├──....
   ├    ├     ├     ├──jdbc
   ├    ├     ├     ├     ├──UUID             # JDBC instance identifier
   ├    ├     ├     ├     ├──....   
   ├    ├     ├──status
   ├    ├     ├     ├──UUID                   
   ├    ├     ├     ├──....
   ├    ├     ├──worker_id
   ├    ├     ├     ├──UUID
   ├    ├     ├     ├──....
   ├    ├     ├──show_process_list_trigger
   ├    ├     ├     ├──process_id:UUID
   ├    ├     ├     ├──....
   ├    ├     ├──labels                      
   ├    ├     ├     ├──UUID
   ├    ├     ├     ├──....               
   ├    ├──storage_nodes                       
   ├    ├     ├──${databaseName.groupName.ds} 
   ├    ├     ├──${databaseName.groupName.ds}
   ├──statistics
   ├    ├──shardingsphere
   ├    ├     ├──schemas
   ├    ├     ├     ├──shardingsphere
   ├    ├     ├     ├     ├──tables             # system tables
   ├    ├     ├     ├     ├     ├──sharding_table_statistics    # sharding statistics table
   ├    ├     ├     ├     ├     ├     ├──8a2dcb0d97c3d86ef77b3d4651a1d7d0  # md5
   ├    ├     ├     ├     ├     ├──cluster_information    # cluster information table
```

### /rules

These are the global rule configurations, transaction configuration.

```
transaction:
  defaultType: XA
  providerType: Atomikos
```

### /props

These are the properties configuration. Please refer to the [Configuration Manual](/en/user-manual/common-config/props/) for more details.

```yaml
kernel-executor-size: 20
sql-show: true
```

### /metadata/${databaseName}/data_sources/ds_0/versions/0

Database connection pools, whose properties (e.g. DBCP, C3P0, Druid and HikariCP) are configured by the users.

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
```

### /metadata/${databaseName}/rules/sharding/tables/t_order/versions/0

Sharding configuration。

```yaml
actualDataNodes: ds_${0..1}.t_order_${0..1}
auditStrategy:
  allowHintDisable: true
  auditorNames:
    - t_order_dml_sharding_conditions_0
databaseStrategy:
  standard:
    shardingAlgorithmName: t_order_database_inline
    shardingColumn: user_id
keyGenerateStrategy:
  column: another_id
  keyGeneratorName: t_order_snowflake
logicTable: t_order
tableStrategy:
  standard:
    shardingAlgorithmName: t_order_table_inline
    shardingColumn: order_id
```

### /metadata/${databaseName}/schemas/${schemaName}/tables/t_order/versions/0

Use separate node storage for each table.

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

It includes running instance information of database access object, with sub-nodes as the identifiers of the currently running instance, which is automatically generated at each startup using UUID.

The identifiers are temporary nodes, which are registered when instances are online and cleared when instances are offline. The registry center monitors the change of those nodes to govern the database access of running instances and other things.

### /nodes/storage_nodes

It can orchestrate replica database, and delete or disable data dynamically.
