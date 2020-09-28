+++
title = "RDL"
weight = 4
+++

## 什么是 RDL?

RDL（Rule Definition Language）是ShardingSphere特有的内置SQL语言。用户可以使用RDL语言向SharidngSphere注入数据源资源、创建分片规则等，即向ShardingSphere注入数据库资源信息和分片规则信息。
RDL使得用户抛弃对传统Yaml或其他配置文件的依赖，像使用数据库一样，通过SQL进行资源信息的注入和规则的配置。

当前，RDL主要包括以下SQL内容：

- `Create DATASOURCES`，用于注入数据源信息。

```sql
// SQL
CREATE DATASOURCES (
ds_key=host_name:host_port:db_name:user_name:pwd
[, ds_key=host_name:host_port:db_name:user_name:pwd, ...]
)

// Example
CREATE datasources (
ds0=127.0.0.1:3306:demo_ds_2:root:pwd, 
ds1=127.0.0.1:3306:demo_ds_3:root:pwd)
```
- `CREATE SHARDINGRULE`，用于配置分片规则。
```sql
// SQL

CREATE SHARDINGRULE (
sharding_table_name=sharding_algorithm(algorithm_property[, algothrim_property])
[, sharding_table_name=sharding_algorithm_type(algorithm_property[, algothrim_property]), ...]
)

sharding_algorithm_type: {MOD | HASH_MODE} 
mod_algorithm_properties: sharding_column,shards_amount
mod_hash_algorithm_properties: sharding_column,shards_amount

// Example
CREATE shardingrules (
t_order=hash_mod(order_id, 4), 
t_item=mod(item_id, 2)
)
```

## RDL使用实战

### 前置工作
1. Start the service of MySQL instances 
2. Create MySQL databases (Viewed as the resources for ShardingProxy)
3. Create a role or user with creating privileges for ShardingProxy
4. Start the service of Zookeeper (For persisting configuration)

### 启动ShardingProxy
1. Add `governance` and `authentication` setting item to the `server.yaml`  (Please refer to the example in this file)
2. Start the ShardingProxy ([Instruction](/en/quick-start/shardingsphere-proxy-quick-start/))

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
CREATE shardingrule (
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

### 注意
1. 当前, `DROP DB`只会移除`逻辑的分布式数据库`，不会删除用户真实的数据库 (**TODO**)。
2. `DROP TABLE`会将逻辑分片表和数据库中真实的表全部删除。
3. `CREATE DB`只会创建`逻辑的分布式数据库`，所以需要用户提前创建好真实的数据库（**TODO**）。
4. `自动分片算法`会持续增加，从而覆盖用户各大分片场景 (**TODO**)。
5. 重构`ShardingAlgorithmPropertiesUtil`（**TODO**）。
6. 保证所有客户端完成RDL执行（**TODO**）。
7. 增加 `ALTER DB` 和 `ALTER TABLE`的支持（**TODO**）。
