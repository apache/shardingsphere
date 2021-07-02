+++
title = "Data Source"
weight = 1
+++

## Definition

```sql
ADD RESOURCE dataSource [, dataSource] ...

dataSource:
    dataSourceName(HOST=hostName,PORT=port,DB=dbName,USER=user [, PASSWORD=password])
    
DROP RESOURCE dataSourceName [, dataSourceName] ...    
```

- Before adding resources, please confirm that a distributed database has been created, and execute the `use` command to successfully select a database
- Confirm that the added resource can be connected normally, otherwise it will not be added successfully
- Duplicate `dataSourceName` is not allowed to be added
- `DROP RESOURCE` will only delete logical resources, not real data sources
- Resources referenced by rules cannot be deleted

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
);

DROP RESOURCE resource_0, resource_1;
```
