+++
title = "DROP RESOURCE"
weight = 3
+++

### Description

The `DROP RESOURCE` syntax is used to drop resources from the current schema

### Syntax
```SQL
DropResource ::=
  'DROP' 'RESOURCE' ( 'IF' 'EXISTS' )? dataSourceName  ( ',' dataSourceName )* ( 'IGNORE' 'SINGLE' 'TABLES' )?
```

### Supplement

- `DROP RESOURCE` will only drop resources in Proxy, the real data source corresponding to the resource will not be dropped
- Unable to drop resources already used by rules
- When the dropped resource contains single table, need to add the `IGNORE SINGLE TABLES` keyword to ignore the single table and drop the resource
- When adding the `IF EXISTS` keyword, the drop operation is performed only if the resource exists

### Example
- Drop a resource
```SQL
DROP RESOURCE ds_0;
```

- Drop multiple resources
```SQL
DROP RESOURCE ds_1, ds_2;
```

- Drop a resource containing single tables
```SQL
DROP RESOURCE ds_1 IGNORE SINGLE TABLES;
```

- Drop the resource if it exists
```SQL
DROP RESOURCE IF EXISTS ds_2;
```

### Reserved word

    DROP、 RESOURCE、 IF、 EXISTS、 IGNORE、 SINGLE、 TABLES

### Related links
- [Reserved word](/en/reference/distsql/syntax/reserved-word/)