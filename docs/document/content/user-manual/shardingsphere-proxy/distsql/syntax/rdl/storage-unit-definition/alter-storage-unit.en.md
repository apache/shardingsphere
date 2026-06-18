+++
title = "ALTER STORAGE UNIT"
weight = 2
+++

### Description

The `ALTER STORAGE UNIT` syntax is used to alter storage units for the currently selected logical database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterStorageUnit ::=
  'ALTER' 'STORAGE' 'UNIT' storageUnitsDefinition (',' checkPrivileges)?

storageUnitsDefinition ::=
  storageUnitDefinition (',' storageUnitDefinition)*

storageUnitDefinition ::=
  storageUnitName '(' ('HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName | 'URL' '=' url) ',' 'USER' '=' user (',' 'PASSWORD' '=' password)? (',' propertiesDefinition)?')'

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

- Before altering the storage units, please confirm that a database exists in Proxy, and execute the `use` command to select a database;
- `ALTER STORAGE UNIT` is not allowed to change the real data source associated with this storageUnit (determined by host, port and db);
- `ALTER STORAGE UNIT` will switch the connection pool. This operation may affect the ongoing business, please use it with caution;
- Please confirm that the storage unit to be altered can be connected successfully, otherwise the altering will fail;
- `PROPERTIES` is optional, used to customize connection pool properties, `key` must be the same as the connection pool property name;
- `CHECK_PRIVILEGES` can be specified to check privileges of the storage unit user. The supported types of `privilegeType` are `SELECT`, `XA`, `PIPELINE`, and `NONE`. The default value is `SELECT`. When `NONE` is included in the type list, the privilege check is skipped.

### Example

- Alter storage unit using HOST & PORT method

```sql
ALTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

- Alter storage unit and set connection pool properties using HOST & PORT method

```sql
ALTER STORAGE UNIT ds_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_1",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10)
);
```

- Alter storage unit and set connection pool properties using URL method

```sql
ALTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
);
```

- Check `SELECT`, `XA` and `PIPELINE` privileges when altering

```sql
ALTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
), CHECK_PRIVILEGES=SELECT,XA,PIPELINE;
```

### Reserved word

`ALTER`, `STORAGE`, `UNIT`, `HOST`, `PORT`, `DB`, `USER`, `PASSWORD`, `PROPERTIES`, `URL`, `CHECK_PRIVILEGES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
