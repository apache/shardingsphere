+++
title = "COUNT READWRITE_SPLITTING RULE"
weight = 3
+++

### Description

The `COUNT READWRITE_SPLITTING RULE` syntax is used to query the number of readwrite splitting rules for specified database.

### Syntax

```sql
CountReadwriteSplittingRule::=
  'COUNT' 'READWRITE_SPLITTING' 'RULE' ('FROM' databaseName)?

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

- Query the number of readwrite splitting rules for specified database.

```sql
COUNT READWRITE_SPLITTING RULE FROM test1;
```

```sql
mysql> COUNT READWRITE_SPLITTING RULE FROM test1;
+---------------------+----------+-------+
| rule_name           | database | count |
+---------------------+----------+-------+
| readwrite_splitting | test1    | 1     |
+---------------------+----------+-------+
1 row in set (0.02 sec)
```

- Query the number of readwrite splitting rules for current database.

```sql
COUNT READWRITE_SPLITTING RULE;
```

```sql
mysql> COUNT READWRITE_SPLITTING RULE;
+---------------------+----------+-------+
| rule_name           | database | count |
+---------------------+----------+-------+
| readwrite_splitting | test1    | 1     |
+---------------------+----------+-------+
1 row in set (0.00 sec)
```

### Reserved word

`COUNT`, `READWRITE_SPLITTING`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
