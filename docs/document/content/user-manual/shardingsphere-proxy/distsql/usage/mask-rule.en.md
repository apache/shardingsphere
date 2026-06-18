+++
title = "MASK"
weight = 4
+++

## Storage unit Operation

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_0",
    USER="root",
    PASSWORD="root"
);
```

## Rule Operation

- Create mask rule

```sql
CREATE MASK RULE t_mask (
    COLUMNS(
        (NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
        (NAME=address,TYPE(NAME='MD5'))
));
```

- Create mask table

```sql
CREATE TABLE `t_mask` (
    `id` int(11) NOT NULL,
    `user_id` varchar(45) DEFAULT NULL,
    `phone_number` varchar(45) DEFAULT NULL,
    `address` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- Alter mask rule

```sql
ALTER MASK RULE t_mask (
    COLUMNS(
        (NAME=user_id,TYPE(NAME='MD5'))
));
```

- Drop mask rule

```sql
DROP MASK RULE t_mask;
```

- Unregister storage unit

```sql
UNREGISTER STORAGE UNIT ds_0;
```

- Drop distributed database

```sql
DROP DATABASE mask_db;
```
