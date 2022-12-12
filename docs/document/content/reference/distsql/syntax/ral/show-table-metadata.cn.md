+++
title = "SHOW TABLE METADATA"
weight = 8
+++

### 描述

`SHOW TABLE METADATA` 语法用于查询表的元数据

### 语法

```sql
ShowTableMetadata ::=
  'SHOW' 'TABLE' 'METADATA' tableName (',' tableName)* ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```

### 返回值说明

| 列          | 说明       |
|-------------|-----------|
| schema_name | 逻辑库名称 |
| table_name  | 表名称    |
| type        | 元数据类型 |
| name        | 元数据名称 |

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE。` 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 查询指定逻辑库中多个表的元数据

```sql
SHOW TABLE METADATA t_order, t_order_1 FROM test1;
```

```sql
mysql> SHOW TABLE METADATA t_order, t_order_1 FROM test1;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order_1  | COLUMN | order_id |
| test1       | t_order_1  | COLUMN | user_id  |
| test1       | t_order_1  | COLUMN | status   |
| test1       | t_order_1  | INDEX  | PRIMARY  |
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
8 rows in set (0.01 sec)
```

- 查询指定逻辑库中单个表的元数据

```sql
SHOW TABLE METADATA t_order FROM test1;
```

```sql
mysql> SHOW TABLE METADATA t_order FROM test1;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
4 rows in set (0.00 sec)
```

- 查询当前逻辑库中多个表的元数据

```sql
SHOW TABLE METADATA t_order, t_order_1;
```

```sql
mysql> SHOW TABLE METADATA t_order, t_order_1;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order_1  | COLUMN | order_id |
| test1       | t_order_1  | COLUMN | user_id  |
| test1       | t_order_1  | COLUMN | status   |
| test1       | t_order_1  | INDEX  | PRIMARY  |
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
8 rows in set (0.00 sec)
```

- 查询当前逻辑库中单个表的元数据

```sql
SHOW TABLE METADATA t_order;
```

```sql
mysql> SHOW TABLE METADATA t_order;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
4 rows in set (0.01 sec)
```

### 保留字

`SHOW`、`TABLE`、`METADATA`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)