+++
title = "SHOW MIGRATION SOURCE STORAGE UNITS"
weight = 6
+++

### 描述

`SHOW MIGRATION SOURCE STORAGE UNITS` 语法用于查询已经注册的数据迁移源存储单元。

### 语法

```sql
ShowStorageUnit ::=
  'SHOW' 'MIGRATION' 'SOURCE' 'STORAGE' 'UNITS'
```

### 返回值说明

| 列        | 说明        |
| --------- | ---------- |
| name      | 存储单元名称 |
| type      | 存储单元类型 |
| host      | 存储单元地址 |
| port      | 存储单元端口 |
| db        | 数据库名称   |
| attribute | 存储单元参数 |

### 示例

- 查询指定逻辑库中未被使用的存储单元

```sql
SHOW MIGRATION SOURCE STORAGE UNITS;
```

```sql
mysql> SHOW MIGRATION SOURCE STORAGE UNITS;
+------+-------+-----------+------+----------------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------+
| name | type  | host      | port | db             | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes |
+------+-------+-----------+------+----------------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------+
| su_1 | MySQL | 127.0.0.1 | 3306 | migration_ds_0 |                                 |                           |                           |               |               |           |                  |
+------+-------+-----------+------+----------------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`SOURCE`、`STORAGE`、`UNITS`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)