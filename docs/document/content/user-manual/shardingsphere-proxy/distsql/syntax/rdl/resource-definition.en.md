+++
title = "Resource Definition"
weight = 1
+++

## Syntax

```sql
ADD RESOURCE resourceDefinition [, resourceDefinition] ...

ALTER RESOURCE resourceDefinition [, resourceDefinition] ...

DROP RESOURCE resourceName [, resourceName] ... [ignore single tables]

resourceDefinition:
    simpleSource | urlSource

simpleSource:
    resourceName(HOST=hostname,PORT=port,DB=dbName,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

urlSource:
    resourceName(URL=url,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

property:
    key=value
```

### Parameters Explained

| Name         | DataType   | Description   |
|:-------------|:-----------|:--------------|
| resourceName | IDENTIFIER | Resource name |
| hostname     | STRING     | Host or IP    |
| port         | INT        | Port          |
| dbName       | STRING     | DB name       |
| url          | STRING     | URL           |
| user         | STRING     | username      |
| password     | STRING     | password      |

### Notes

- Before adding resources, please confirm that a distributed database has been created, and execute the `use` command to successfully select a database;
- Confirm that the resource to be added or altered can be connected, otherwise the operation will not be successful;
- Duplicate `resourceName` is not allowed;
- `PROPERTIES` is used to customize connection pool parameters, `key` and `value` are both STRING types;
- `ALTER RESOURCE` is not allowed to change the real data source associated with this resource;
- `ALTER RESOURCE` will switch the connection pool. This operation may affect the ongoing business, please use it with caution;
- `DROP RESOURCE` will only delete logical resources, not real data sources;
- Resources referenced by rules cannot be deleted;
- If the resource is only referenced by `single table rule`, and the user confirms that the restriction can be ignored, the optional parameter `ignore single tables` can be added to perform forced deletion.

## Example

```sql
ADD RESOURCE resource_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db0",
    USER="root",
    PASSWORD="root"
),resource_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db1",
    USER="root"
),resource_2 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db2",
    USER="root",
    PROPERTIES("maximumPoolSize"="10")
),resource_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

ALTER RESOURCE resource_0 (
    HOST="127.0.0.1",
    PORT=3309,
    DB="db0",
    USER="root",
    PASSWORD="root"
),resource_1 (
    URL="jdbc:mysql://127.0.0.1:3309/db1?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

DROP RESOURCE resource_0, resource_1;
DROP RESOURCE resource_2, resource_3 ignore single tables;
```
