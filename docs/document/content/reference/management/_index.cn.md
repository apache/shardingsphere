+++
pre = "<b>7.3. </b>"
title = "管控"
weight = 3
+++

## 注册中心数据结构

在定义的命名空间下，`rules` 、`props` 和 `metadata` 节点以 YAML 格式存储配置，可通过修改节点来实现对于配置的动态管理。
`nodes` 存储数据库访问对象运行节点，用于区分不同数据库访问实例。
`statistics` 存储系统表中的数据记录。

```
namespace
   ├──rules                                             # 全局规则配置
   ├     ├──transaction
   ├     ├     ├──active_version                                     
   ├     ├     ├──versions  
   ├     ├     ├     ├──0       
   ├──props                                              # 属性配置
   ├     ├──active_verison                                     
   ├     ├──versions  
   ├     ├     ├──0                  
   ├──metadata                                           # Metadata 配置
   ├     ├──${databaseName} 
   ├     ├     ├──data_sources                          
   ├     ├     ├     ├──units 							 # 存储单元结构配置
   ├     ├     ├     ├    ├──${dataSourceName}                        
   ├     ├     ├     ├    ├     ├──active_verison             # 激活版本                                 
   ├     ├     ├     ├    ├     ├──versions                   # 版本号
   ├     ├     ├     ├    ├     ├     ├──0
   ├     ├     ├     ├    ├──...   
   ├     ├     ├     ├──nodes 							 # 存储节点结构配置
   ├     ├     ├     ├    ├──${dataSourceName}                        
   ├     ├     ├     ├    ├     ├──active_verison             # 激活版本                                
   ├     ├     ├     ├    ├     ├──versions                   # 版本号
   ├     ├     ├     ├    ├     ├     ├──0
   ├     ├     ├     ├    ├──...                             
   ├     ├     ├──schemas                                   # Schema 列表   
   ├     ├     ├     ├──${schemaName}                    
   ├     ├     ├     ├     ├──tables                     # 表结构配置
   ├     ├     ├     ├     ├     ├──${tableName}         
   ├     ├     ├     ├     ├     ├     ├──active_verison # 激活版本                                 
   ├     ├     ├     ├     ├     ├     ├──versions       # 版本号
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...  
   ├     ├     ├     ├     ├──views                      # 视图结构配置
   ├     ├     ├     ├     ├     ├──${viewName}
   ├     ├     ├     ├     ├     ├     ├──active_verison # 激活版本                           
   ├     ├     ├     ├     ├     ├     ├──versions       # 版本号
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...  
   ├     ├     ├──rules
   ├     ├     ├     ├──sharding
   ├     ├     ├     ├     ├──algorithms
   ├     ├     ├     ├     ├     ├──${algorithmName}     # algorithm 名称
   ├     ├     ├     ├     ├     ├     ├──active_verison # 激活版本                           
   ├     ├     ├     ├     ├     ├     ├──versions       # 版本号
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...
   ├     ├     ├     ├     ├──key_generators
   ├     ├     ├     ├     ├     ├──${keyGeneratorName}  # keyGenerator名称
   ├     ├     ├     ├     ├     ├     ├──active_verison # 激活版本                           
   ├     ├     ├     ├     ├     ├     ├──versions       # 版本号
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...         
   ├     ├     ├     ├     ├──tables
   ├     ├     ├     ├     ├     ├──${tableName}         # 逻辑表名称
   ├     ├     ├     ├     ├     ├     ├──active_verison # 激活版本                           
   ├     ├     ├     ├     ├     ├     ├──versions       # 版本号
   ├     ├     ├     ├     ├     ├     ├     ├──0
   ├     ├     ├     ├     ├     ├──...          
   ├──nodes
   ├    ├──compute_nodes
   ├    ├     ├──online
   ├    ├     ├     ├──proxy
   ├    ├     ├     ├     ├──UUID             # Proxy 实例唯一标识
   ├    ├     ├     ├     ├──....
   ├    ├     ├     ├──jdbc
   ├    ├     ├     ├     ├──UUID             # JDBC 实例唯一标识
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
   ├    ├──qualified_data_sources                       
   ├    ├     ├──${databaseName.groupName.dataSourceName} 
   ├    ├     ├──${databaseName.groupName.dataSourceName}
   ├──statistics
   ├    ├──databases
   ├    ├     ├──shardingsphere
   ├    ├     ├     ├──schemas
   ├    ├     ├     ├     ├──shardingsphere
   ├    ├     ├     ├     ├     ├──tables # 系统表
   ├    ├     ├     ├     ├     ├   ├──cluster_information    # 集群信息表
```

### /rules

全局规则配置，事务配置。

```
transaction:
  defaultType: XA
  providerType: Atomikos
```

### /props

属性配置，详情请参见[配置手册](/cn/user-manual/common-config/props/)。

```yaml
kernel-executor-size: 20
sql-show: true
```

### /metadata/${databaseName}/data_sources/units/ds_0/versions/0

数据库连接池的，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid，HikariCP）。

```yaml
ds_0:
  initializationFailTimeout: 1
  validationTimeout: 5000
  maxLifetime: 1800000
  leakDetectionThreshold: 0
  minimumIdle: 1
  password: root
  idleTimeout: 60000
  standardJdbcUrl: jdbc:mysql://127.0.0.1:3306/ds_0?serverTimezone=UTC&useSSL=false
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  maximumPoolSize: 50
  connectionTimeout: 30000
  username: root
  poolName: HikariPool-1
```
### /metadata/${databaseName}/data_sources/nodes/ds_0/versions/0

数据库连接池的，不同数据库连接池属性自适配（例如：HikariCP）。

```yaml
ds_0:
  initializationFailTimeout: 1
  validationTimeout: 5000
  maxLifetime: 1800000
  leakDetectionThreshold: 0
  minimumIdle: 1
  password: root
  idleTimeout: 60000
  standardJdbcUrl: jdbc:mysql://127.0.0.1:3306/ds_0?serverTimezone=UTC&useSSL=false
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  maximumPoolSize: 50
  connectionTimeout: 30000
  username: root
  poolName: HikariPool-1
```

### /metadata/${databaseName}/rules/sharding/tables/t_order/versions/0

分片规则配置。

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

表结构配置，每个表使用单独节点存储。

```yaml
name: t_order                             # 表名
columns:                                  # 列
  id:                                     # 列名
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
indexs:                                   # 索引
  t_user_order_id_index:                  # 索引名
    name: t_user_order_id_index
```

### /nodes/compute_nodes

数据库访问对象运行实例信息，子节点是当前运行实例的标识。
运行实例标识使用 UUID 生成，每次启动重新生成。
运行实例标识均为临时节点，当实例上线时注册，下线时自动清理。
注册中心监控这些节点的变化来治理运行中实例对数据库的访问等。

### /nodes/qualified_data_sources

可以治理读写分离从库，可动态禁用。
