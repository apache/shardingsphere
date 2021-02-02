+++
title = "RDL"
weight = 1
+++

## 定义

RDL（Resource & Rule Definition Language）用于定义数据源资源、创建规则等。

RDL 主要包括以下 SQL 内容：

- `Create DATASOURCES`，用于注入数据源信息。

```sql
// SQL
CREATE DATASOURCES (
ds_key=host_name:host_port:db_name:user_name:pwd
[, ds_key=host_name:host_port:db_name:user_name:pwd, ...]
)

// Example
CREATE datasources (
ds0=127.0.0.1:3306:demo_ds_0:root:pwd, 
ds1=127.0.0.1:3306:demo_ds_1:root:pwd)
```
- `CREATE SHARDING RULE`，用于配置分片规则。
```sql
// SQL

CREATE SHARDING RULE (
sharding_table_name=sharding_algorithm(algorithm_property[, algothrim_property])
[, sharding_table_name=sharding_algorithm_type(algorithm_property[, algothrim_property]), ...]
)

sharding_algorithm_type: {MOD | HASH_MODE} 
mod_algorithm_properties: sharding_column,shards_amount
mod_hash_algorithm_properties: sharding_column,shards_amount

// Example
CREATE SHARDING RULE (
t_order=hash_mod(order_id, 4), 
t_item=mod(item_id, 2)
)
```

## 使用实战

### 前置工作

1. 启动MySQL服务
2. 创建MySQL数据库(参考ShardingProxy数据源配置规则)
3. 为ShardingProxy创建一个拥有创建权限的角色或者用户
4. 启动Zookeeper服务 (为了持久化配置)

### 启动ShardingProxy

1. 添加 `governance` 和 `authentication` 配置参数到 `server.yaml` (请参考相关example案例)
2. 启动 ShardingProxy ([相关介绍](/cn/quick-start/shardingsphere-proxy-quick-start/))

### 创建分布式数据库和分片表

1. 连接到ShardingProxy
2. 创建分布式数据库

```SQL
CREATE DATABASE sharding_db;
```

3. 使用新创建的数据库

```SQL
USE sharding_db;
```

2. 配置数据源信息

```SQL
CREATE datasources (
ds0=127.0.0.1:3306:demo_ds_2:root:pwd, 
ds1=127.0.0.1:3306:demo_ds_3:root:pwd)
```

3. 创建分片规则

```SQL
CREATE SHARDING RULE (
t_order=hash_mod(order_id, 4), 
t_item=mod(item_id, 2)
)
```

这里的 `hash_mode` 和 `mod`是自动分片算法的Key。详情请查阅 [auto-sharding-algorithm](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding/)。

4. 创建切分表

```SQL
CREATE TABLE `t_order` (
  `order_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `t_item` (
  `item_id` int NOT NULL,
  `order_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

5. 删除切分表

```SQL
DROP TABLE t_order;
DROP TABLE t_item;
```

6. 删除分布式数据库

```SQL
DROP DATABASE sharding_db
```

### 注意事项

1. 当前, `DROP DB`只会移除`逻辑的分布式数据库`，不会删除用户真实的数据库 (**TODO**)。
2. `DROP TABLE`会将逻辑分片表和数据库中真实的表全部删除。
3. `CREATE DB`只会创建`逻辑的分布式数据库`，所以需要用户提前创建好真实的数据库（**TODO**）。
4. `自动分片算法`会持续增加，从而覆盖用户各大分片场景 (**TODO**)。
5. 重构`ShardingAlgorithmPropertiesUtil`（**TODO**）。
6. 保证所有客户端完成RDL执行（**TODO**）。
7. 增加 `ALTER DB` 和 `ALTER TABLE`的支持（**TODO**）。
