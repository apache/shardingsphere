+++
title = "REGISTER STORAGE UNIT"
weight = 1
+++

### Description

The `REGISTER STORAGE UNIT` syntax is used to register storage unit for the currently selected logical database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
RegisterStorageUnit ::=
  'REGISTER' 'STORAGE' 'UNIT' ifNotExists? storageUnitsDefinition (',' checkPrivileges)?

storageUnitsDefinition ::=
  storageUnitDefinition (',' storageUnitDefinition)*

storageUnitDefinition ::=
  storageUnitName '(' ('HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName | 'URL' '=' url) ',' 'USER' '=' user (',' 'PASSWORD' '=' password)? (',' propertiesDefinition)?')'

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

storageUnitName ::=
  identifier

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

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal

checkPrivileges ::=
  'CHECK_PRIVILEGES' '=' privilegeType (',' privilegeType)*

privilegeType ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- Before register storage units, please confirm that a database has been created in Proxy, and execute the `use` command to successfully select a database;
- Confirm that the registered storage unit can be connected normally, otherwise it will not be added successfully;
- `storageUnitName` is case-sensitive;
- `storageUnitName` needs to be unique within the current database;
- `storageUnitName` name only allows letters, numbers and `_`, and must start with a letter;
- `PROPERTIES` is optional, used to customize connection pool properties, `key` must be the same as the connection pool
  property name;
- `CHECK_PRIVILEGES` can be specified to check privileges of the storage unit user. The supported types of `privilegeType` are `SELECT`, `XA`, `PIPELINE`, and `NONE`. The default value is `SELECT`. When `NONE` is included in the type list, the privilege check is skipped.

### Example

- Register storage unit using HOST & PORT method

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

- Register storage unit and set connection pool properties using HOST & PORT method

```sql
REGISTER STORAGE UNIT ds_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_1",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10)
);
```

- Register storage unit and set connection pool properties using URL method

```sql
REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
);
```

- Register storage unit with `ifNotExists` clause

```sql
REGISTER STORAGE UNIT IF NOT EXISTS ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

- Check `SELECT`, `XA` and `PIPELINE` privileges when registering

```sql
REGISTER STORAGE UNIT ds_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db_3?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
), CHECK_PRIVILEGES=SELECT,XA,PIPELINE;
```

### Reserved word

`REGISTER`, `STORAGE`, `UNIT`, `HOST`, `PORT`, `DB`, `USER`, `PASSWORD`, `PROPERTIES`, `URL`, `CHECK_PRIVILEGES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
