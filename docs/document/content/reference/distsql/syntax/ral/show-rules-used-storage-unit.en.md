+++
title = "SHOW RULES USED STORAGE UNIT"
weight = 11
+++

### Description

The `SHOW RULES USED STORAGE UNIT` syntax is used to query the rules for using the specified storage unit in specified database.

### Syntax

```sql
ShowRulesUsedStorageUnit ::=
  'SHOW' 'RULES' 'USED' 'STORAGE' 'UNIT' storageUnitName ('FROM' databaseName)?

storageUnitName ::=
  identifier

databaseName ::=
  identifier
```

### Return Value Description

| Columns     | Description   |
|-------------|---------------|
| type        | rule type     |
| name        | rule name     |

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Query the rules for using the specified storage unit in specified database

```sql
SHOW RULES USED STORAGE UNIT su_1 FROM test1;
```

```sql
mysql> SHOW RULES USED STORAGE UNIT su_1 FROM test1;
+---------------------+------------+
| type                | name       |
+---------------------+------------+
| readwrite_splitting | ms_group_0 |
| readwrite_splitting | ms_group_0 |
+---------------------+------------+
2 rows in set (0.01 sec)
```

- Query the rules for using the specified storage unit in current database

```sql
SHOW RULES USED STORAGE UNIT su_1;
```

```sql
mysql> SHOW RULES USED STORAGE UNIT su_1;
+---------------------+------------+
| type                | name       |
+---------------------+------------+
| readwrite_splitting | ms_group_0 |
| readwrite_splitting | ms_group_0 |
+---------------------+------------+
2 rows in set (0.01 sec)
```

### Reserved word

`SHOW`, `RULES`, `USED`, `STORAGE`, `UNIT`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
