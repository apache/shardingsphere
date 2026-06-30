+++
title = "COUNT SINGLE TABLE"
weight = 3
+++

### Description

The `COUNT SINGLE TABLE` syntax is used to query the number of single tables for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountSingleTable::=
  'COUNT' 'SINGLE' 'TABLE' ('FROM' databaseName)?
  
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

### Return Value Description

| Column    | Description                                         |
|-----------|-----------------------------------------------------|
| rule_name | Single rule name                                    |
| database  | The database name where the single table is located |
| count     | The count of single table                           |

### Example

- Query the number of single tables for specified database.

```sql
COUNT SINGLE TABLE FROM sharding_db;
``` 

```sql
mysql> COUNT SINGLE TABLE FROM sharding_db;
+-----------+-------------+-------+
| rule_name | database    | count |
+-----------+-------------+-------+
| single    | sharding_db | 2     |
+-----------+-------------+-------+
1 row in set (0.02 sec)
```

### Reserved word

`COUNT`, `SINGLE`, `TABLE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
