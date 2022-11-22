+++
pre = "<b>7.3. </b>"
title = "Management"
weight = 3
+++

## Data Structure in Registry Center

Under defined namespace, `rules`, `props` and `metadata` nodes persist in YAML, modifying nodes can dynamically refresh configurations. `nodes` node persist the runtime node of database access object, to distinguish different database access instances.

```
namespace
   ├──rules                                   # Global rule configuration
   ├──props                                   # Properties configuration
   ├──metadata                                # Metadata configuration
   ├     ├──${databaseName}                   # Logic database name
   ├     ├     ├──schemas                     # Schema list   
   ├     ├     ├     ├──${schemaName}         # Logic schema name
   ├     ├     ├     ├     ├──tables          # Table configuration
   ├     ├     ├     ├     ├     ├──${tableName} 
   ├     ├     ├     ├     ├     ├──...  
   ├     ├     ├     ├     ├──views          # View configuration
   ├     ├     ├     ├     ├     ├──${viewName} 
   ├     ├     ├     ├     ├     ├──...  
   ├     ├     ├     ├──...    
   ├     ├     ├──versions                    # Metadata version list      
   ├     ├     ├     ├──${versionNumber}      # Metadata version
   ├     ├     ├     ├     ├──data_sources     # Data source configuration
   ├     ├     ├     ├     ├──rules           # Rule configuration  
   ├     ├     ├     ├──...
   ├     ├     ├──active_version              # Active metadata version
   ├     ├──...      
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
   ├    ├     ├──process_trigger
   ├    ├     ├     ├──process_list_id:UUID
   ├    ├     ├     ├──....
   ├    ├     ├──labels                      
   ├    ├     ├     ├──UUID
   ├    ├     ├     ├──....               
   ├    ├──storage_nodes                       
   ├    ├     ├──${databaseName.groupName.ds} 
   ├    ├     ├──${databaseName.groupName.ds}
```

### /rules

Global rule configuration, which can include transaction configuration, SQL parser configuration, etc.

```yaml
- !TRANSACTION
  defaultType: XA
  providerType: Atomikos
- !SQL_PARSER
  sqlCommentParseEnabled: true
```

### /props

Properties configuration. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-jdbc/props/) for more details.

```yaml
kernel-executor-size: 20
sql-show: true
```

### /metadata/${databaseName}/versions/${versionNumber}/dataSources

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

### /metadata/${databaseName}/versions/${versionNumber}/rules

Rule configurations, including sharding, readwrite-splitting, data encryption, shadow DB configurations.

```yaml
- !SHARDING
  xxx
  
- !READWRITE_SPLITTING
  xxx
  
- !ENCRYPT
  xxx
```

### /metadata/${databaseName}/schemas/${schemaName}/tables

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

It includes running instance information of database access object, with sub-nodes as the identifiers of currently running instance, which is automatically generated at each startup using UUID. Those identifiers are temporary nodes, which are registered when instances are on-line and cleared when instances are off-line. The registry center monitors the change of those nodes to govern the database access of running instances and other things.

### /nodes/storage_nodes

It is able to orchestrate replica database, delete or disable data dynamically.
