+++
pre = "<b>7.3. </b>"
title = "管控"
weight = 3
+++

## 注册中心数据结构

在定义的命名空间下，`rules` 、`props` 和 `metadata` 节点以 YAML 格式存储配置，可通过修改节点来实现对于配置的动态管理。`nodes` 存储数据库访问对象运行节点，用于区分不同数据库访问实例。

```
namespace
   ├──rules                                   # 全局规则配置
   ├──props                                   # 属性配置
   ├──metadata                                # Metadata 配置
   ├     ├──${databaseName}                   # 逻辑数据库名称
   ├     ├     ├──schemas                     # Schema 列表   
   ├     ├     ├     ├──${schemaName}         # 逻辑 Schema 名称
   ├     ├     ├     ├     ├──tables          # 表结构配置
   ├     ├     ├     ├     ├     ├──${tableName} 
   ├     ├     ├     ├     ├     ├──...  
   ├     ├     ├     ├──...    
   ├     ├     ├──versions                    # 元数据版本列表      
   ├     ├     ├     ├──${versionNumber}      # 元数据版本号
   ├     ├     ├     ├     ├──dataSources     # 数据源配置
   ├     ├     ├     ├     ├──rules           # 规则配置   
   ├     ├     ├     ├──...
   ├     ├     ├──active_version              # 激活的元数据版本号
   ├     ├──...      
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

全局规则配置，可包括访问 ShardingSphere-Proxy 用户名和密码的权限配置。

```yaml
- !AUTHORITY
users:
  - root@%:root
  - sharding@127.0.0.1:sharding
provider:
  type: ALL_PERMITTED
```

### /props

属性配置，详情请参见[配置手册](/cn/user-manual/shardingsphere-jdbc/props/)。

```yaml
kernel-executor-size: 20
sql-show: true
```

### /metadata/${databaseName}/versions/${versionNumber}/dataSources

多个数据库连接池的集合，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid，HikariCP）。

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

规则配置，可包括数据分片、读写分离、数据加密、影子库压测等配置。

```yaml
- !SHARDING
  xxx
  
- !READWRITE_SPLITTING
  xxx
  
- !ENCRYPT
  xxx
```

### /metadata/${databaseName}/schemas/${schemaName}/tables

表结构配置，每个表使用单独节点存储，暂不支持动态修改。

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

### /nodes/storage_nodes

可以治理读写分离从库，可动态添加删除以及禁用。
