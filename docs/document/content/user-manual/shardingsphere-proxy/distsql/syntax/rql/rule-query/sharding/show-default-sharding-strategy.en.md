+++
title = "SHOW DEFAULT SHARDING STRATEGY"
weight = 4
+++

### Description

The `SHOW DEFAULT SHARDING STRATEGY` syntax is used to query default sharding strategy in specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowDefaultShardingStrategy::=
  'SHOW' 'DEFAULT' 'SHARDING' 'STRATEGY' ('FROM' databaseName)?

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column                   | Description                   |
|--------------------------|-------------------------------|
| name                     | Sharding strategy scope       |
| type                     | Sharding strategy type        |
| sharding_column          | Sharding column               |
| sharding_algorithm_name  | Sharding algorithm name       |
| sharding_algorithm_type  | Sharding algorithm type       |
| sharding_algorithm_props | Sharding algorithm properties |

### Example

- Query default sharding strategy in specified database.

```sql
SHOW DEFAULT SHARDING STRATEGY FROM sharding_db;
```

```sql
mysql> SHOW DEFAULT SHARDING STRATEGY FROM sharding_db;
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| name     | type     | sharding_column | sharding_algorithm_name | sharding_algorithm_type | sharding_algorithm_props                            |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| TABLE    | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
| DATABASE | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
2 rows in set (0.00 sec)
```

- Query default sharding strategy in current database.

```sql
SHOW DEFAULT SHARDING STRATEGY;
```

```sql
mysql> SHOW DEFAULT SHARDING STRATEGY;
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| name     | type     | sharding_column | sharding_algorithm_name | sharding_algorithm_type | sharding_algorithm_props                            |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| TABLE    | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
| DATABASE | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
2 rows in set (0.00 sec)
```

### Reserved word

`SHOW`, `DEFAULT`, `SHARDING`, `STRATEGY`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
