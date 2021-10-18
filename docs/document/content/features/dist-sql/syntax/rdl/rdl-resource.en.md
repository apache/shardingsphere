+++
title = "Data Source"
weight = 1
+++

## Definition

```sql
ADD RESOURCE dataSource [, dataSource] ...

ALTER RESOURCE dataSource [, dataSource] ...

dataSource:
    simpleSource | urlSource

simpleSource:
    dataSourceName(HOST=hostName,PORT=port,DB=dbName,USER=user [,PASSWORD=password] [,PROPERTIES(poolProperty [,poolProperty] ...)])

urlSource:
    dataSourceName(URL=url,USER=user [,PASSWORD=password] [,PROPERTIES(poolProperty [,poolProperty]) ...])

poolProperty:
    "key"= ("value" | value)
    
DROP RESOURCE dataSourceName [, dataSourceName] ... [ignore single tables]
```

- Before adding resources, please confirm that a distributed database has been created, and execute the `use` command to successfully select a database
- Confirm that the added resource can be connected normally, otherwise it will not be added successfully
- Duplicate `dataSourceName` is not allowed to be added
- In the definition of a `dataSource`, the syntax of `simpleSource` and `urlSource` cannot be mixed
- `poolProperty` is used to customize connection pool properties, `key` must be the same as the connection pool property name, `value` supports int and String types
- `ALTER RESOURCE` will switch the connection pool. This operation may affect the ongoing business, please use it with caution
- `DROP RESOURCE` will only delete logical resources, not real data sources
- Resources referenced by rules cannot be deleted
- If the resource is only referenced by `single table rule`, and the user confirms that the restriction can be ignored, the optional parameter `ignore single tables` can be added to perform forced deletion

## Example

```sql
ADD RESOURCE resource_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db0,
    USER=root,
    PASSWORD=root
),resource_1 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db1,
    USER=root
),resource_2 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db2,
    USER=root,
    PROPERTIES("maximumPoolSize"=10)
),resource_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);

ALTER RESOURCE resource_0 (
    HOST=127.0.0.1,
    PORT=3309,
    DB=db0,
    USER=root,
    PASSWORD=root
),resource_1 (
    URL="jdbc:mysql://127.0.0.1:3309/db1?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
)

DROP RESOURCE resource_0, resource_1;
DROP RESOURCE resource_2, resource_3 ignore single tables;
```
