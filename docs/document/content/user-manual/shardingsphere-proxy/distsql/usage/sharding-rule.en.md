+++
title = "Sharding"
weight = 1
+++

## Resource Operation

- Configure data source information

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

## Rule Operation

- Create sharding rule

```sql
CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="4")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

- Create sharding table

```sql
CREATE TABLE `t_order` (
  `order_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

- Drop sharding table

```sql
DROP TABLE t_order;
```

- Drop sharding rule

```sql
DROP SHARDING TABLE RULE t_order;
```

- Drop storage unit

```sql
UNREGISTER STORAGE UNIT ds_0, ds_1;
```

- Drop distributed database

```sql
DROP DATABASE foo_db;
```
