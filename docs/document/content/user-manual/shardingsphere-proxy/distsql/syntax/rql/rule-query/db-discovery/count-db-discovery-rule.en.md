+++
title = "COUNT DB_DISCOVERY RULE"
weight = 5
+++

### Description

The `COUNT DB_DISCOVERY RULE` syntax is used to query the number of database discovery rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountDBDiscoveryRule::=
  'COUNT' 'DB_DISCOVERY' 'RULE' ('FROM' databaseName)?

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

| Column    | Description                             |
| ----------| ----------------------------------------|
| rule_name | rule type                               |
| database  | the database to which the rule belongs  |
| count     | the number of the rule                  |


### Example

- Query the number of database discovery rules for specified database.

```sql
COUNT DB_DISCOVERY RULE FROM discovery_db;
```

```sql
mysql> COUNT DB_DISCOVERY RULE FROM discovery_db;
+--------------+-----------------+-------+
| rule_name    | database        | count |
+--------------+-----------------+-------+
| db_discovery | discovery_db    | 1     |
+--------------+-----------------+-------+
1 row in set (0.00 sec)
```

- Query the number of database discovery rules for current database.

```sql
COUNT DB_DISCOVERY RULE;
```

```sql
mysql> COUNT DB_DISCOVERY RULE;
+--------------+-----------------+-------+
| rule_name    | database        | count |
+--------------+-----------------+-------+
| db_discovery | discovery_db    | 1     |
+--------------+-----------------+-------+
1 row in set (0.00 sec)
```

### Reserved word

`COUNT`, `DB_DISCOVERY`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
