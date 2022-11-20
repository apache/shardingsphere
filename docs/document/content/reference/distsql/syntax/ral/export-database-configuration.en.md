+++
title = "EXPORT DATABASE CONFIGURATION"
weight = 9
+++

### Description

The `EXPORT DATABASE CONFIGURATION` syntax is used to export storage units and rule configurations to `YAML` format.

### Syntax

```sql
ExportDatabaseConfiguration ::=
  'EXPORT' 'DATABASE' 'CONFIGURATION' ('FROM' databaseName)? ('TO' 'FILE' filePath)?

databaseName ::=
  identifier

filePath ::=
  string
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

- When `filePath` is not specified, the storage units and rule configurations will export to screen.
### Example

- Export storage units and rule configurations from specified database to specified file path

```sql
EXPORT DATABASE CONFIGURATION FROM test1 TO FILE "/xxx/config_test1.yaml";
```

- Export storage units and rule configurations from specified database to screen

```sql
EXPORT DATABASE CONFIGURATION FROM test1;
```

```sql
mysql> EXPORT DATABASE CONFIGURATION FROM test1;
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| result                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| databaseName: test1
dataSources:
  su_1:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/migration_ds_0
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 50
  su_2:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/db1
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 50
rules:
 |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```

- Export storage units and rule configurations from current database to specified file path

```sql
EXPORT DATABASE CONFIGURATION TO FILE "/xxx/config_test1.yaml";
```

- Export storage units and rule configurations from current database to screen

```sql
EXPORT DATABASE CONFIGURATION;
```

```sql
mysql> EXPORT DATABASE CONFIGURATION;
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| result                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| databaseName: test1
dataSources:
  su_1:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/migration_ds_0
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 50
  su_2:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/db1
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 50
rules:
 |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`EXPORT`, `DATABASE`, `CONFIGURATION`, `FROM`, `TO`, `FILE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
