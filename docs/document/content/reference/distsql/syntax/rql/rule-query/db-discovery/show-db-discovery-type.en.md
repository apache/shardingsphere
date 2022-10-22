+++
title = "SHOW DB_DISCOVERY TYPES"
weight = 3
+++

### Description

The `SHOW DB_DISCOVERY TYPES` syntax is used to query database discovery types for specified database.

### Syntax

```
ShowDatabaseDiscoveryType::=
  'SHOW' 'DB_DISCOVERY' 'TYPES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column                   | Description                        |
| ------------------------ | -----------------------------------|
| name                     | Database discovery type name       |
| type                     | Database discovery type category   |
| props                    | Database discovery type properties |




### Example

- Query database discovery types for specified database.

```sql
SHOW DB_DISCOVERY TYPES FROM test1;
```

```sql
mysql> SHOW DB_DISCOVERY TYPES FROM test1;
+-------------------+-----------+---------------------------------------------------+
| name              | type      | props                                             |
+-------------------+-----------+---------------------------------------------------+
| group_0_MySQL.MGR | MySQL.MGR | {group-name=667edd3c-02ec-11ea-9bb3-080027e39bd2} |
+-------------------+-----------+---------------------------------------------------+
1 row in set (0.01 sec)
```

- Query database discovery types for current database.

```sql
SHOW DB_DISCOVERY TYPES;
```

```sql
mysql> SHOW DB_DISCOVERY TYPES;
+-------------------+-----------+---------------------------------------------------+
| name              | type      | props                                             |
+-------------------+-----------+---------------------------------------------------+
| group_0_MySQL.MGR | MySQL.MGR | {group-name=667edd3c-02ec-11ea-9bb3-080027e39bd2} |
+-------------------+-----------+---------------------------------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `DB_DISCOVERY`, `TYPES`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
