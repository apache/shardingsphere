+++
title = "RDL Syntax"
weight = 2
+++


## ADD RESOURCE

---
The `ADD RESOURCE` syntax is used to add resources for the currently selected schema.



### Syntax
```SQL
AddResource ::=
  'ADD' 'RESOURCE' dataSource (',' dataSource)*

dataSource ::=
  dataSourceName '(' ( 'HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName  |  'URL' '=' url  ) ',' 'USER' '=' user (',' 'PASSWORD' '=' password )?  (',' 'PROPERTIES'  '(' ( key  '=' value ) ( ',' key  '=' value )* ')'  )?')'

dataSourceName ::=
  identifier

hostname ::=
  identifier

dbName ::=
  identifier

port ::=
  int

password ::=
  identifier | int | string 

user ::=
  identifier | number

url ::=
  identifier | string

```

 ### Supplement
- Before adding resources, please confirm that a schema has been created in Proxy, and execute the `use` command to successfully select a schema
- Confirm that the added resource can be connected normally, otherwise it will not be added successfully
- `dataSourceName` is case-sensitive
- `dataSourceName` needs to be unique within the current schema
- `dataSourceName` name only allows letters, numbers and `_`, and must start with a letter
- `poolProperty` is used to customize connection pool parameters, `key` must be the same as the connection pool parameter name, `value` supports int and String types
- String mode is recommended when `password` contains special characters

 ### Example
- Add resources using standard mode
```SQL
ADD RESOURCE ds_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_0,
    USER=root,
    PASSWORD=root
);
```

- Add resources and set connection pool parameters using standard mode
```SQL
ADD RESOURCE ds_1 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_1,
    USER=root,
    PASSWORD=root
    PROPERTIES("maximumPoolSize"=10)
);
```

- Add resources and set connection pool parameters using URL patterns
```SQL
ADD RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```

### Reserved word
- Datasource reserved words

    SYS、MYSQL、INFORMATION_SCHEMA、PERFORMANCE_SCHEMA

- Standard reserved words 

    ADD、RESOURCE、HOST、PORT、DB、USER、PASSWORD、PROPERTIES、URL

 ### Related links
- [Reserved word](/en/reference/distsql/syntax/reserved-word/)