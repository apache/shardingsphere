+++
title = "SHOW SINGLE TABLE"
weight = 1
+++

### 描述

`SHOW SINGLE TABLE` 语法用于查询指定逻辑库中的单表。

### 语法

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowSingleTable::=
  'SHOW' 'SINGLE' ('TABLES' ('LIKE' likeLiteral)?|'TABLE' tableName) ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                | 说明              |
|-------------------|-------------------|
| table_name        | 单表名称           |
| storage_unit_name | 单表所在的数据源名称 |


### 示例

- 查询指定逻辑库中的指定单表

```sql
SHOW SINGLE TABLE t_user FROM sharding_db;
```

```sql
mysql> SHOW SINGLE TABLE t_user FROM sharding_db;
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
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_user     | ds_0              |
+------------+-------------------+
1 row in set (0.00 sec)
```

- 查询指定逻辑库中的单表

```sql
SHOW SINGLE TABLES FROM sharding_db;
```

```sql
mysql> SHOW SINGLE TABLES FROM sharding_db;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_user     | ds_0              |
+------------+-------------------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中的单表

```sql
SHOW SINGLE TABLES;
```

```sql
mysql> SHOW SINGLE TABLES;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_user     | ds_0              |
+------------+-------------------+
1 row in set (0.00 sec)
```

- 查询指定逻辑库中表名以 `order_5` 结尾的单表

```sql
SHOW SINGLE TABLES LIKE '%order_5' FROM sharding_db;
```

```sql
mysql> SHOW SINGLE TABLES LIKE '%order_5' FROM sharding_db;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_order_5  | ds_1              |
+------------+-------------------+
1 row in set (0.11 sec)
```

- 查询当前逻辑库中表名以 `order_5` 结尾的单表

```sql
SHOW SINGLE TABLES LIKE '%order_5';
```

```sql
mysql> SHOW SINGLE TABLES LIKE '%order_5';
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_order_5  | ds_1              |
+------------+-------------------+
1 row in set (0.11 sec)
```

### 保留字

`SHOW`、`SINGLE`、`TABLE`、`TABLES`、`LIKE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

