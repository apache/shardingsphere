+++
title = "ALTER RESOURCE"
weight = 3
+++

### Description

The `ALTER RESOURCE` syntax is used to alter resources for the currently selected database.

### Syntax

```sql
AlterResource ::=
  'ALTER' 'RESOURCE' resourceDefinition (',' resourceDefinition)*

resourceDefinition ::=
  resourceName '(' ( 'HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName  |  'URL' '=' url  ) ',' 'USER' '=' user (',' 'PASSWORD' '=' password )?  (',' proerties)?')'

resourceName ::=
  string

hostname ::=
  string
    
port ::=
  int

dbName ::=
  string

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

- Before altering the resources, please confirm that a database exists in Proxy, and execute the `use` command to
  successfully select a database;
- `ALTER RESOURCE` is not allowed to change the real data source associated with this resource;
- `ALTER RESOURCE` will switch the connection pool. This operation may affect the ongoing business, please use it with
  caution;
- `resourceName` is case-sensitive;
- `resourceName` needs to be unique within the current database;
- `resourceName` name only allows letters, numbers and `_`, and must start with a letter;
- `poolProperty` is used to customize connection pool parameters, `key` must be the same as the connection pool
  parameter name, `value` supports int and String types;
- When `password` contains special characters, it is recommended to use the string form; for example, the string form
  of `password@123` is `"password@123"`.

### Example

- Alter resource using standard mode

```sql
ALTER RESOURCE ds_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_0,
    USER=root,
    PASSWORD=root
);
```

- Alter resource and set connection pool parameters using standard mode

```sql
ALTER RESOURCE ds_1 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_1,
    USER=root,
    PASSWORD=root
    PROPERTIES("maximumPoolSize"=10)
);
```

- Alter resource and set connection pool parameters using URL patterns

```sql
ALTER RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```

### Reserved word

`ALTER`, `RESOURCE`, `HOST`, `PORT`, `DB`, `USER`, `PASSWORD`, `PROPERTIES`, `URL`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
