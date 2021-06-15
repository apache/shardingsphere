+++
title = "注册中心"
weight = 1
+++

## 实现动机

- 配置集中化：越来越多的运行时实例，使得散落的配置难于管理，配置不同步导致的问题十分严重。将配置集中于配置中心，可以更加有效进行管理。

- 配置动态化：配置修改后的分发，是配置中心可以提供的另一个重要能力。它可支持数据源和规则的动态切换。

- 存放运行时的动态/临时状态数据，比如可用的 ShardingSphere 的实例，需要禁用或熔断的数据源等。

- 提供熔断数据库访问程序对数据库的访问和禁用从库的访问的编排治理能力。治理模块仍然有大量未完成的功能（比如流控等）。

## 注册中心数据结构

在定义的命名空间下， `rules` 、 `props` 和 `metadata` 节点以 YAML 格式存储配置，可通过修改节点来实现对于配置的动态管理。 `states` 存储数据库访问对象运行节点，用于区分不同数据库访问实例。

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
   ├──states
   ├    ├──proxynodes
   ├    ├     ├──${your_instance_ip_a}@${your_instance_pid_x}@${UUID}
   ├    ├     ├──${your_instance_ip_b}@${your_instance_pid_y}@${UUID}
   ├    ├     ├──....
   ├    ├──datanodes
   ├    ├     ├──${schema_1}
   ├    ├     ├      ├──${ds_0}
   ├    ├     ├      ├──${ds_1}
   ├    ├     ├──${schema_2}
   ├    ├     ├      ├──${ds_0}
   ├    ├     ├      ├──${ds_1}
   ├    ├     ├──....
```

### /rules

全局规则配置，可包括访问 ShardingSphere-Proxy 用户名和密码的权限配置。

```yaml
- !AUTHORITY
users:
  - root@%:root
  - sharding@127.0.0.1:sharding
provider:
  type: NATIVE
```

### /props

属性配置，详情请参见[配置手册](/cn/user-manual/shardingsphere-jdbc/configuration/)。

```yaml
executor-size: 20
sql-show: true
```

### /metadata/${schemeName}/dataSources

多个数据库连接池的集合，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid, HikariCP）。

```yaml
ds_0: 
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  props:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    maintenanceIntervalMilliseconds: 30000
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
    maintenanceIntervalMilliseconds: 30000
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
```

### /metadata/${schemeName}/rules

规则配置，可包括数据分片、读写分离、数据加密、影子库压测等配置。

```yaml
- !SHARDING
  xxx
  
- !READWRITE_SPLITTING
  xxx
  
- !ENCRYPT
  xxx
```

### /metadata/${schemeName}/schema

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

### /states/proxynodes

数据库访问对象运行实例信息，子节点是当前运行实例的标识。
运行实例标识由运行服务器的 IP 地址和 PID 构成。运行实例标识均为临时节点，当实例上线时注册，下线时自动清理。
注册中心监控这些节点的变化来治理运行中实例对数据库的访问等。

### /states/datanodes

可以治理读写分离从库，可动态添加删除以及禁用。

## 动态生效

在注册中心上修改、删除、新增相关配置，会动态推送到生产环境并立即生效。

## 操作指南

### 熔断实例

可在 `IP地址@PID@UUID` 节点写入 `DISABLED`（忽略大小写）表示禁用该实例，删除 `DISABLED` 表示启用。

Zookeeper 命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/states/proxynodes/${your_instance_ip_a}@${your_instance_pid_x}@${UUID} DISABLED
```

### 禁用从库

在读写分离场景下，可在数据源名称子节点中写入 `DISABLED`（忽略大小写）表示禁用从库数据源，删除 `DISABLED` 或节点表示启用。

Zookeeper 命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/states/datanodes/${your_schema_name}/${your_replica_datasource_name} DISABLED
```
