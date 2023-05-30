+++
title = "数据脱敏"
weight = 4
+++

## 存储单元操作

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_0",
    USER="root",
    PASSWORD="root"
);
```

## 规则操作

- 创建数据脱敏规则

```sql
CREATE MASK RULE t_mask (
    COLUMNS(
        (NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
        (NAME=address,TYPE(NAME='MD5'))
));
```

- 创建数据脱敏表

```sql
CREATE TABLE `t_mask` (
    `id` int(11) NOT NULL,
    `user_id` varchar(45) DEFAULT NULL,
    `phone_number` varchar(45) DEFAULT NULL,
    `address` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- 修改数据脱敏规则

```sql
ALTER MASK RULE t_mask (
    COLUMNS(
        (NAME=user_id,TYPE(NAME='MD5'))
));
```

- 删除数据脱敏规则

```sql
DROP MASK RULE t_mask;
```

- 移除数据源

```sql
UNREGISTER STORAGE UNIT ds_0;
```

- 删除分布式数据库

```sql
DROP DATABASE mask_db;
```
