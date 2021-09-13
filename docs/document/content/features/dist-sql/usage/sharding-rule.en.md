+++
title = "Sharding"
weight = 1
+++

## Usage

### Pre-work

1. Start the MySQL service
2. Create MySQL database (refer to ShardingProxy data source configuration rules)
3. Create a role or user with creation permission for ShardingProxy
4. Start Zookeeper service (for persistent configuration)

### Start ShardingProxy

1. Add `mode` and `authentication` configurations to `server.yaml` (please refer to the example of ShardingProxy)
2. Start ShardingProxy ([Related introduction](/en/quick-start/shardingsphere-proxy-quick-start/))

### Create a distributed database and sharding tables

1. Connect to ShardingProxy
2. Create a distributed database

```SQL
CREATE DATABASE sharding_db;
```

3. Use newly created database

```SQL
USE sharding_db;
```

2. Configure data source information

```SQL
ADD RESOURCE ds_0 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_1,
USER=root,
PASSWORD=root
);

ADD RESOURCE ds_1 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_2,
USER=root,
PASSWORD=root
);
```

3. Create sharding rules

```SQL
CREATE SHARDING TABLE RULE t_order(
RESOURCES(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=4)),
GENERATED_KEY(COLUMN=order_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);
```

4. Create sharding table

```SQL
CREATE TABLE `t_order` (
  `order_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

5. Drop sharding table

```SQL
DROP TABLE t_order;
```

6. Drop sharding rule

```SQL
DROP SHARDING TABLE RULE t_order;
```

7. Drop resource

```SQL
DROP RESOURCE ds_0, ds_1;
```

8. Drop distributed database

```SQL
DROP DATABASE sharding_db;
```

### Notice

1. Currently, `DROP DATABASE` will only remove the `logical distributed database`, not the user's actual database (**TODO**).
2. `DROP TABLE` will delete all logical fragmented tables and actual tables in the database.
3. `CREATE DATABASE` will only create a `logical distributed database`, so users need to create actual databases in advance (**TODO**).
4. The `Auto Sharding Algorithm` will continue to increase to cover the user's various sharding scenarios (**TODO**).
5. Refactor `ShardingAlgorithmPropertiesUtil`(**TODO**).
6. Ensure that all clients complete RDL execution (**TODO**).
7. Add support for `ALTER DATABASE` and `ALTER TABLE` (**TODO**).
