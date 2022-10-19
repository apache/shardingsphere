+++
title = "SHOW SHARDING AUDITORS"
weight = 8

+++

### Description

`SHOW SHARDING AUDITORS` syntax is used to query sharding auditors in specified database.

### Syntax

```
ShowShardingAuditors::=
  'SHOW' 'SHARDING' 'AUDITOR'('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| column                 | Description                    |
| -----------------------| -------------------------------|
| name                   | Sharding auditor name          |
| type                   | Sharding auditor type          |
| props                  | Sharding auditor properties    |

### Example

- Query sharding auditors for the specified logical database

```sql
SHOW SHARDING AUDITORS FROM test1;
```

```sql
mysql> SHOW SHARDING AUDITORS FROM test1;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions | {}    |
+-------------------------------+-------------------------+-------+
1 row in set (0.01 sec)
```

- Query sharding auditors for the current logical database

```sql
SHOW SHARDING AUDITORS;
```

```sql
mysql> SHOW SHARDING AUDITORS;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions | {}    |
+-------------------------------+-------------------------+-------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`、`SHARDING`、`AUDITORS`、`FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)

