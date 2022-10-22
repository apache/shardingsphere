+++
title = "Storage Unit Definition"
weight = 1
+++

## Syntax

```sql
REGISTER STORAGE UNIT storageUnitDefinition [, storageUnitDefinition] ...

ALTER STORAGE UNIT storageUnitDefinition [, storageUnitDefinition] ...

UNREGISTER STORAGE UNIT storageUnitName [, storageUnitName] ... [ignore single tables]

storageUnitDefinition:
    simpleSource | urlSource

simpleSource:
    storageUnitName(HOST=hostname,PORT=port,DB=dbName,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

urlSource:
    storageUnitName(URL=url,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

property:
    key=value
```

### Parameters Explained

| Name             | DataType   | Description       |
|:-----------------|:-----------|:------------------|
| storageUnitName  | IDENTIFIER | Storage unit name |
| hostname         | STRING     | Host or IP        |
| port             | INT        | Port              |
| dbName           | STRING     | DB name           |
| url              | STRING     | URL               |
| user             | STRING     | username          |
| password         | STRING     | password          |

### Notes

- Before adding storage unit, please confirm that a distributed database has been created, and execute the `use` command to successfully select a database;
- Confirm that the storage unit to be added or altered can be connected, otherwise the operation will not be successful;
- Duplicate `storageUnitName` is not allowed;
- `PROPERTIES` is used to customize connection pool parameters, `key` and `value` are both STRING types;
- `ALTER RESOURCE` is not allowed to change the real data source associated with this storage unit;
- `ALTER RESOURCE` will switch the connection pool. This operation may affect the ongoing business, please use it with caution;
- `DROP RESOURCE` will only delete logical storage unit, not real storage unit;
- Storage unit referenced by rules cannot be deleted;
- If the storage unit is only referenced by `single table rule`, and the user confirms that the restriction can be ignored, the optional parameter `ignore single tables` can be added to perform forced deletion.

## Example

```sql
REGISTER STORAGE UNIT storage_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db0",
    USER="root",
    PASSWORD="root"
),storage_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db1",
    USER="root"
),storage_2 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db2",
    USER="root",
    PROPERTIES("maximumPoolSize"="10")
),storage_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

ALTER STORAGE UNIT storage_0 (
    HOST="127.0.0.1",
    PORT=3309,
    DB="db0",
    USER="root",
    PASSWORD="root"
),storage_1 (
    URL="jdbc:mysql://127.0.0.1:3309/db1?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

UNREGISTER STORAGE UNIT storage_0, storage_1;
UNREGISTER STORAGE UNIT storage_2, storage_3 ignore single tables;
```
