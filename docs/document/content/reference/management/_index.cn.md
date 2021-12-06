+++
pre = "<b>7.1. </b>"
title = "管控"
weight = 1
+++

## 注册中心数据结构

在定义的命名空间下，`rules` 、`props` 和 `metadata` 节点以 YAML 格式存储配置，可通过修改节点来实现对于配置的动态管理。`status` 存储数据库访问对象运行节点，用于区分不同数据库访问实例。

```
namespace
   ├──rules                                     # 全局规则配置
   ├──props                                     # 属性配置
   ├──metadata                                  # Metadata 配置
   ├      ├──${schema_1}                        # Schema 名称1
   ├      ├      ├──dataSources                 # 数据源配置
   ├      ├      ├──rules                       # 规则配置
   ├      ├      ├──schema                      # 表结构配置
   ├      ├──${schema_2}                        # Schema 名称2
   ├      ├      ├──dataSources                 # 数据源配置
   ├      ├      ├──rules                       # 规则配置
   ├      ├      ├──schema                      # 表结构配置
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

全局规则配置，可包括访问 ShardingSphere-Proxy 用户名和密码的权限配置。

```yaml
- !AUTHORITY
users:
  - root@%:root
  - sharding@127.0.0.1:sharding
provider:
  type: ALL_PRIVILEGES_PERMITTED
```

### /props

属性配置，详情请参见[配置手册](/cn/user-manual/shardingsphere-jdbc/props/)。

```yaml
kernel-executor-size: 20
sql-show: true
```

### /metadata/${schemaName}/dataSources

多个数据库连接池的集合，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid, HikariCP）。

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

### /metadata/${schemaName}/rules

规则配置，可包括数据分片、读写分离、数据加密、影子库压测等配置。

```yaml
- !SHARDING
  xxx
  
- !READWRITE_SPLITTING
  xxx
  
- !ENCRYPT
  xxx
```

### /metadata/${schemaName}/schema

表结构配置，暂不支持动态修改。

```yaml
tables:                                       # 表
  t_order:                                    # 表名
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

数据库访问对象运行实例信息，子节点是当前运行实例的标识。
运行实例标识由运行服务器的 IP 地址和 PORT 构成。
运行实例标识均为临时节点，当实例上线时注册，下线时自动清理。
注册中心监控这些节点的变化来治理运行中实例对数据库的访问等。

### /status/storage_nodes

可以治理读写分离从库，可动态添加删除以及禁用。
