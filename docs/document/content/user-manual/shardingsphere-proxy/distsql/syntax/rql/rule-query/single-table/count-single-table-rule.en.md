+++
title = "COUNT SINGLE_TABLE RULE"
weight = 4
+++

### Description

The `COUNT SINGLE_TABLE RULE` syntax is used to query number of single table rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountSingleTableRule::=
  'COUNT' 'SINGLE_TABLE' 'RULE' ('FROM' databaseName)?
  
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
| rule_name | Single table rule name                              |
| database  | The database name where the single table is located |
| count     | The count of single table rules                     |

### Example

- Query the number of single table rules for specified database.

```sql
COUNT SINGLE_TABLE RULE
``` 

```sql
mysql> COUNT SINGLE_TABLE RULE;
+--------------+----------+-------+
| rule_name    | database | count |
+--------------+----------+-------+
| t_single_0   | ds       | 2     |
+--------------+----------+-------+
1 row in set (0.02 sec)
```

### Reserved word

`COUNT`, `SINGLE_TABLE`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
