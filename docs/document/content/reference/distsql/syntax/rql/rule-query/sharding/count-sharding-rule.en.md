+++
title = "COUNT SHARDING RULE"
weight = 16
+++

### Description

The `COUNT SHARDING RULE` syntax is used to query the number of sharding rules for specified database.

### Syntax

```sql
CountShardingRule::=
  'COUNT' 'SHARDING' 'RULE' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column    | Description                             |
| ----------| ----------------------------------------|
| rule_name | rule type                               |
| database  | the database to which the rule belongs  |
| count     | the number of the rule                  |


### Example

- Query the number of sharding rules for specified database.

```sql
COUNT SHARDING RULE FROM test1;
```

```sql
mysql> COUNT SHARDING RULE FROM test1;
+--------------------------+----------+-------+
| rule_name                | database | count |
+--------------------------+----------+-------+
| sharding_table           | test1    | 2     |
| sharding_table_reference | test1    | 2     |
| broadcast_table          | test1    | 0     |
+--------------------------+----------+-------+
3 rows in set (0.00 sec)
```

- Query the number of sharding rules for current database.

```sql
COUNT SHARDING RULE;
```

```sql
mysql> COUNT SHARDING RULE;
+--------------------------+----------+-------+
| rule_name                | database | count |
+--------------------------+----------+-------+
| sharding_table           | test1    | 2     |
| sharding_table_reference | test1    | 2     |
| broadcast_table          | test1    | 0     |
+--------------------------+----------+-------+
3 rows in set (0.00 sec)
```

### Reserved word

`COUNT`, `SHARDING`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
