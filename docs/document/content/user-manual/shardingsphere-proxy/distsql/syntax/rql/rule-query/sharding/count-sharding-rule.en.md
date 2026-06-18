+++
title = "COUNT SHARDING RULE"
weight = 15
+++

### Description

The `COUNT SHARDING RULE` syntax is used to query the number of sharding rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountShardingRule::=
  'COUNT' 'SHARDING' 'RULE' ('FROM' databaseName)?

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

- Query the number of sharding rules for specified database.

```sql
COUNT SHARDING RULE FROM sharding_db;
```

```sql
mysql> COUNT SHARDING RULE FROM sharding_db;
+--------------------------+----------------+-------+
| rule_name                | database       | count |
+--------------------------+----------------+-------+
| sharding_table           | sharding_db    | 2     |
| sharding_table_reference | sharding_db    | 2     |
+--------------------------+----------------+-------+
2 rows in set (0.00 sec)
```

- Query the number of sharding rules for current database.

```sql
COUNT SHARDING RULE;
```

```sql
mysql> COUNT SHARDING RULE;
+--------------------------+----------------+-------+
| rule_name                | database       | count |
+--------------------------+----------------+-------+
| sharding_table           | sharding_db    | 2     |
| sharding_table_reference | sharding_db    | 2     |
+--------------------------+----------------+-------+
2 rows in set (0.00 sec)
```

### Reserved word

`COUNT`, `SHARDING`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
