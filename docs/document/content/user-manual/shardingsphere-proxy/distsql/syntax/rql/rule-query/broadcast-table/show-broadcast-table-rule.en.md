+++
title = "SHOW BROADCAST TABLE RULE"
weight = 1
+++

## Description

The `SHOW BROADCAST TABLE RULE` syntax is used to broadcast tables for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowBroadcastTableRule ::=
  'SHOW' 'BROADCAST' 'TABLE' 'RULES' ('FROM' databaseName)? 

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

| Column          | Description          |
|-----------------|----------------------|
| broadcast_table | Broadcast table name |

### Example

- Query broadcast tables for specified database.

```sql
SHOW BROADCAST TABLE RULES FROM sharding_db;
```

```sql
mysql> SHOW BROADCAST TABLE RULES FROM sharding_db;
+-----------------+
| broadcast_table |
+-----------------+
| t_a             |
| t_b             |
| t_c             |
+-----------------+
3 rows in set (0.00 sec)
```

- Query broadcast table for current database.

```sql
SHOW BROADCAST TABLE RULES;
```

```sql
mysql> SHOW BROADCAST TABLE RULES;
+-----------------+
| broadcast_table |
+-----------------+
| t_a             |
| t_b             |
| t_c             |
+-----------------+
3 rows in set (0.00 sec)
```

### Reserved word

`SHOW`, `BROADCAST`, `TABLE`, `RULES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
