+++
title = "COUNT ENCRYPT RULE"
weight = 4
+++

### Description

The `COUNT ENCRYPT RULE` syntax is used to query the number of encrypt rules for specified database.

### Syntax

```sql
CountEncryptRule::=
  'COUNT' 'ENCRYPT' 'RULE' ('FROM' databaseName)?

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

- Query the number of encrypt rules for specified database.

```sql
COUNT ENCRYPT RULE FROM test1;
```

```sql
mysql> COUNT ENCRYPT RULE FROM test1;
+-----------+----------+-------+
| rule_name | database | count |
+-----------+----------+-------+
| encrypt   | test1    | 2     |
+-----------+----------+-------+
1 row in set (0.01 sec)
```

- Query the number of encrypt rules for current database.

```sql
COUNT ENCRYPT RULE;
```

```sql
mysql> COUNT ENCRYPT RULE;
+-----------+----------+-------+
| rule_name | database | count |
+-----------+----------+-------+
| encrypt   | test1    | 2     |
+-----------+----------+-------+
1 row in set (0.01 sec)
```

### Reserved word

`COUNT`, `ENCRYPT`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
