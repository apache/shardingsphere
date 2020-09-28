+++
title = "RDL"
weight = 4
+++

## What's the RDL?
RDL means rule definition language to create your distributed DBs, tables by SQL. In other words, this is a specific SQLs for ShardingSphere to inject some configurations.
By means of RDL, users could inject data sources and configure sharding rules by SQL.

At present, there are the following RDLs,
- `Create DATASOURCES` to add database resources to ShardingSphere

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
- `CREATE SHARDINGRULE` to create sharding rule made by table rules
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

## A common scenario for RDL

### Pre-work
1. Start the service of MySQL instances 
2. Create MySQL databases (Viewed as the resources for ShardingProxy)
3. Create a role or user with creating privileges for ShardingProxy
4. Start the service of Zookeeper (For persisting configuration)

### Initialize ShardingProxy
1. Add `governance` and `authentication` setting item to the `server.yaml`  (Please refer to the example in this file)
2. Start the ShardingProxy ([Instruction](/en/quick-start/shardingsphere-proxy-quick-start/))

### Create Sharding DBs and Tables
1. Connect to ShardingProxy
2. Create a sharding database

```SQL
CREATE DATABASE sharding_db;
```

3. Use the sharding database

```SQL
USE sharding_db;
```

2. Add database resources for this sharding DB

```SQL
CREATE datasources (
ds0=127.0.0.1:3306:demo_ds_2:root:pwd, 
ds1=127.0.0.1:3306:demo_ds_3:root:pwd)
```

3. Create Sharding rule

```SQL
CREATE shardingrule (
t_order=hash_mod(order_id, 4), 
t_item=mod(item_id, 2)
)
```

Here `hash_mode` and `mod` are auto sharding algorithm. Please visit [auto-sharding-algorithm](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding/) to learn more.

4. Create tables 

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

5. Drop sharding tables

```SQL
DROP TABLE t_order;
DROP TABLE t_item;
```

6. Drop sharding database

```SQL
DROP DATABASE sharding_db
```

### Notices
1. Currently, `DROP DB` only removes` the logic sharding schema` instead of removing the actual databases in MySQL instance (**Improved point in the future**).
2. `DROP TABLE` will drop `the logic sharding table` and `the corresponding actual tables` in MySQL instance together.
3. `CREATE DB` just create the logic sharding schema rather than create the actual databases in MySQL instance (**Improved point in the future**).
4. Add more `auto sharding algorithms`
5. Improvement in `ShardingAlgorithmPropertiesUtil`.
