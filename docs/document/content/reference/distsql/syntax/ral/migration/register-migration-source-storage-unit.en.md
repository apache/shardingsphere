+++
title = "REGISTER MIGRATION SOURCE STORAGE UNIT"
weight = 4
+++

### Description

The `REGISTER MIGRATION SOURCE STORAGE UNIT` syntax is used to register migration source storage unit for the currently connection.

### Syntax

```sql
RegisterStorageUnit ::=
  'REGISTER' 'MIGRATION' 'SOURCE' 'STORAGE' 'UNIT' storageUnitDefinition (',' storageUnitDefinition)*

storageUnitDefinition ::=
  StorageUnitName '('  'URL' '=' url ',' 'USER' '=' user (',' 'PASSWORD' '=' password )?  (',' proerties)?')'

storageUnitName ::=
  identifier

url ::=
  string

user ::=
  string

password ::=
  string

proerties ::=
  PROPERTIES '(' property ( ',' property )* ')'

property ::=
  key '=' value

key ::=
  string

value ::=
  string
```

### Supplement

- Confirm that the registered migration source storage unit can be connected normally, otherwise it will not be added successfully;
- `storageUnitName` is case-sensitive;
- `storageUnitName` needs to be unique within the current connection;
- `storageUnitName` name only allows letters, numbers and `_`, and must start with a letter;
- `poolProperty` is used to customize connection pool parameters, `key` must be the same as the connection pool
  parameter name, `value` supports int and String types;
- When `password` contains special characters, it is recommended to use the string form; For example, the string form
  of `password@123` is `"password@123"`.
- The data migration source storage unit currently only supports registration using `URL`, and temporarily does not support using `HOST` and `PORT`.

### Example

- Register migration source storage unit

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT su_0 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_su_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root"
);
```

- Register migration source storage unit and set connection pool parameters

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT su_0 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_su_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

### Reserved word

`REGISTER`, `MIGRATION`, `SOURCE`, `STORAGE`, `UNIT`, `USER`, `PASSWORD`, `PROPERTIES`, `URL`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
