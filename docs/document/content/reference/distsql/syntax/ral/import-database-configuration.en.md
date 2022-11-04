+++
title = "IMPORT DATABASE CONFIGURATION"
weight = 10
+++

### Description

The `IMPORT DATABASE CONFIGURATION` syntax is used to import `YAML` configuration to specified database.

### Syntax

```sql
ExportDatabaseConfiguration ::=
  'IMPORT' 'DATABASE' 'CONFIGURATION' 'FROM' filePath ('TO' databaseName)?

databaseName ::=
  identifier

filePath ::=
  string
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

- The `IMPORT DATABASE CONFIGURATION` syntax only supports import operations on empty database.

### Example

- Import the configuration in `YAML` into the specified database

```sql
IMPORT DATABASE CONFIGURATION FROM "/xxx/config_test1.yaml" TO test1;
```

- Import the configuration in `YAML` into the current database

```sql
IMPORT DATABASE CONFIGURATION FROM "/xxx/config_test1.yaml";
```

### Reserved word

`IMPORT`, `DATABASE`, `CONFIGURATION`, `FROM`, `TO`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
