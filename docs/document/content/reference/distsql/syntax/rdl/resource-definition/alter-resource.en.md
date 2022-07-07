+++
title = "ALTER RESOURCE"
weight = 3
+++


### Description

The `ALTER RESOURCE` syntax is used to alter resources for the currently selected database.



### Syntax
```SQL
AlterResource ::=
  'ALTER' 'RESOURCE' dataSource (',' dataSource)*

dataSource ::=
  dataSourceName '(' ( 'HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName  |  'URL' '=' url  ) ',' 'USER' '=' user (',' 'PASSWORD' '=' password )?  (',' 'PROPERTIES'  '(' ( key  '=' value ) ( ',' key  '=' value )* ')'  )?')'

dataSourceName ::=
  identifier

hostname ::=
  identifier | ip

dbName ::=
  identifier

port ::=
  int

password ::=
  identifier | int | string 

user ::=
  identifier

url ::=
  identifier | string

```

 ### Supplement
- Before altering the resources, please confirm that a database exists in Proxy, and execute the `use` command to successfully select a database
- `dataSourceName` is case-sensitive
- `dataSourceName` needs to be unique within the current database
- `dataSourceName` name only allows letters, numbers and `_`, and must start with a letter
- `poolProperty` is used to customize connection pool parameters, `key` must be the same as the connection pool parameter name, `value` supports int and String types
- When `password` contains special characters, it is recommended to use the string form; for example, the string form of `password@123` is `"password@123"`

 ### Example
- Alter resource using standard mode
```SQL
ALTER RESOURCE ds_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_0,
    USER=root,
    PASSWORD=root
);
```

- Alter resource and set connection pool parameters using standard mode
```SQL
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
```SQL
ALTER RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```

### Reserved word

    ALTER, RESOURCE, HOST, PORT, DB, USER, PASSWORD, PROPERTIES, URL

 ### Related links
- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
