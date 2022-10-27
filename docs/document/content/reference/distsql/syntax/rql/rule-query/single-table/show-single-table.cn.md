+++
title = "SHOW SINGLE TABLE"
weight = 3
+++

### 描述

`SHOW SINGLE TABLE` 语法用于查询指定逻辑库中的单表。

### 语法

```
ShowSingleTable::=
  'SHOW' 'SINGLE' ('TABLES'|'TABLE' tableName) ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列            | 说明                  |
| ------------- | -------------------- |
| table_name    | 单表名称              |
| resource_name | 单表所在的数据源名称    |


### 示例

- 查询指定逻辑库中的指定单表

```sql
SHOW SINGLE TABLE t_user FROM test1;
```

```sql
mysql> SHOW SINGLE TABLE t_user FROM test1;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中的指定单表

```sql
SHOW SINGLE TABLE t_user;
```

```sql
mysql> SHOW SINGLE TABLE t_user;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

- 查询指定逻辑库中的单表

```sql
SHOW SINGLE TABLES FROM test1;
```

```sql
mysql> SHOW SINGLE TABLES FROM test1;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中的单表

```sql
SHOW SINGLE TABLES;
```

```sql
mysql> SHOW SINGLE TABLES;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`SINGLE`、`TABLE`、`TABLES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

