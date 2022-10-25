+++
title = "数据分片"
weight = 1
+++

## 资源操作

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_1",
    USER="root",
    PASSWORD="root"
),ds_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_2",
    USER="root",
    PASSWORD="root"
);
```

## 规则操作

- 创建分片规则

```sql
CREATE SHARDING TABLE RULE t_order(
RESOURCES(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="4")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

- 创建切分表

```sql
CREATE TABLE `t_order` (
  `order_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

- 删除切分表

```sql
DROP TABLE t_order;
```

- 删除分片规则

```sql
DROP SHARDING TABLE RULE t_order;
```

- 删除数据源

```sql
UNREGISTER STORAGE UNIT ds_0, ds_1;
```

- 删除分布式数据库

```sql
DROP DATABASE foo_db;
```
