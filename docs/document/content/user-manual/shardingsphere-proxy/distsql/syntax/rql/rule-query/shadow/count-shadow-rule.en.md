+++
title = "COUNT SHADOW RULE"
weight = 5
+++

### Description

The `COUNT SHADOW RULE` syntax is used to query the number of shadow rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountShadowRule::=
  'COUNT' 'SHADOW' 'RULE' ('FROM' databaseName)?

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

- Query the number of shadow rules for specified database.

```sql
COUNT SHADOW RULE FROM shadow_db;
```

```sql
mysql> COUNT SHADOW RULE FROM shadow_db;
+-----------+--------------+-------+
| rule_name | database     | count |
+-----------+--------------+-------+
| shadow    | shadow_db    | 1     |
+-----------+--------------+-------+
1 row in set (0.00 sec)
```

- Query the number of shadow rules for current database.

```sql
COUNT SHADOW RULE;
```

```sql
mysql> COUNT SHADOW RULE;
+-----------+--------------+-------+
| rule_name | database     | count |
+-----------+--------------+-------+
| shadow    | shadow_db    | 1     |
+-----------+--------------+-------+
1 row in set (0.01 sec)
```

### Reserved word

`COUNT`, `SHADOW`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
