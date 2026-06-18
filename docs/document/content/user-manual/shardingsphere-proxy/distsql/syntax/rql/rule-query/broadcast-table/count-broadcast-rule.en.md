+++
title = "COUNT BROADCAST RULE"
weight = 2
+++

### Description

The `COUNT BROADCAST RULE` syntax is used to query the number of broadcast table rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountBroadcastRule::=
  'COUNT' 'BROADCAST' 'RULE' ('FROM' databaseName)?

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

- Query the number of broadcast table rules for specified database.

```sql
COUNT BROADCAST RULE FROM sharding_db;
```

```sql
mysql> COUNT BROADCAST RULE FROM sharding_db;
+--------------------------+----------------+-------+
| rule_name                | database       | count |
+--------------------------+----------------+-------+
| broadcast_table          | sharding_db    | 0     |
+--------------------------+----------------+-------+
1 rows in set (0.00 sec)
```

- Query the number of broadcast table rules for current database.

```sql
COUNT BROADCAST RULE;
```

```sql
mysql> COUNT BROADCAST RULE;
+--------------------------+----------------+-------+
| rule_name                | database       | count |
+--------------------------+----------------+-------+
| broadcast_table          | sharding_db    | 0     |
+--------------------------+----------------+-------+
1 rows in set (0.00 sec)
```

### Reserved word

`COUNT`, `BROADCAST`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
