+++
title = "COUNT READWRITE_SPLITTING RULE"
weight = 2
+++

### Description

The `COUNT READWRITE_SPLITTING RULE` syntax is used to query the number of readwrite-splitting rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountReadwriteSplittingRule::=
  'COUNT' 'READWRITE_SPLITTING' 'RULE' ('FROM' databaseName)?

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

- Query the number of readwrite-splitting rules for specified database.

```sql
COUNT READWRITE_SPLITTING RULE FROM readwrite_splitting_db;
```

```sql
mysql> COUNT READWRITE_SPLITTING RULE FROM readwrite_splitting_db;
+---------------------+---------------------------+-------+
| rule_name           | database                  | count |
+---------------------+---------------------------+-------+
| readwrite_splitting | readwrite_splitting_db    | 1     |
+---------------------+---------------------------+-------+
1 row in set (0.02 sec)
```

- Query the number of readwrite-splitting rules for current database.

```sql
COUNT READWRITE_SPLITTING RULE;
```

```sql
mysql> COUNT READWRITE_SPLITTING RULE;
+---------------------+---------------------------+-------+
| rule_name           | database                  | count |
+---------------------+---------------------------+-------+
| readwrite_splitting | readwrite_splitting_db    | 1     |
+---------------------+---------------------------+-------+
1 row in set (0.00 sec)
```

### Reserved word

`COUNT`, `READWRITE_SPLITTING`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
