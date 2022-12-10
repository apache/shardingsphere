+++
title = "SHOW SHARDING KEY GENERATORS"
weight = 6
+++

### Description

`SHOW SHARDING KEY GENERATORS` syntax is used to query sharding key generators in specified database.

### Syntax

```
ShowShardingKeyGenerators::=
  'SHOW' 'SHARDING' 'KEY' 'GENERATOR'('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| column                 | Description                          |
| -----------------------| -------------------------------------|
| name                   | Sharding key generator name          |
| type                   | Sharding key generator type          |
| props                  | Sharding key generator properties    |

### Example

- Query the sharding key generators of the specified logical database

```sql
SHOW SHARDING KEY GENERATORS FROM test1;
```

```sql
mysql> SHOW SHARDING KEY GENERATORS FROM test1;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake | {}    |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

- Query the sharding key generators of the current logical database

```sql
SHOW SHARDING KEY GENERATORS;
```

```sql
mysql> SHOW SHARDING KEY GENERATORS;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake | {}    |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `KEY`, `GENERATORS`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)

