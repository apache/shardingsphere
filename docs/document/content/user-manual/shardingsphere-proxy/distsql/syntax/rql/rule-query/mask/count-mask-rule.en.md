+++
title = "COUNT MASK RULE"
weight = 2
+++

### Description

The `COUNT MASK RULE` syntax is used to query the number of mask rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountMaskRule::=
  'COUNT' 'MASK' 'RULE' ('FROM' databaseName)?

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

| Column    | Description                            |
|-----------|----------------------------------------|
| rule_name | rule type                              |
| database  | the database to which the rule belongs |
| count     | the number of the rule                 |


### Example

- Query the number of mask rules for specified database.

```sql
COUNT MASK RULE FROM mask_db;
```

```sql
mysql> COUNT MASK RULE FROM mask_db;
+-----------+----------+-------+
| rule_name | database | count |
+-----------+----------+-------+
| mask      | mask_db  | 3     |
+-----------+----------+-------+
1 row in set (0.50 sec)
```

- Query the number of mask rules for current database.

```sql
COUNT MASK RULE;
```

```sql
mysql> COUNT MASK RULE;
+-----------+----------+-------+
| rule_name | database | count |
+-----------+----------+-------+
| mask      | mask_db  | 3     |
+-----------+----------+-------+
1 row in set (0.50 sec)
```

### Reserved word

`COUNT`, `MASK`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
