+++
title = "DROP RESOURCE"
weight = 4
+++

### Description

The `DROP RESOURCE` syntax is used to drop resources from the current database

### Syntax

```sql
DropResource ::=
  'DROP' 'RESOURCE' ( 'IF' 'EXISTS' )? resourceName  ( ',' resourceName )* ( 'IGNORE' 'SINGLE' 'TABLES' )?

resourceName ::=
  string
```

### Supplement

- `DROP RESOURCE` will only drop resources in Proxy, the real data source corresponding to the resource will not be
  dropped;
- Unable to drop resources already used by rules. `Resources are still in used.` will be prompted when removing
  resources used by rules;
- The resource need to be removed only contains `SINGLE TABLE RULE`, and when the user confirms that this restriction
  can be ignored, the `IGNORE SINGLE TABLES` keyword can be added to remove the resource.

### Example

- Drop a resource

```sql
DROP RESOURCE ds_0;
```

- Drop multiple resources

```sql
DROP RESOURCE ds_1, ds_2;
```

- Ignore single table rule remove resource

```sql
DROP RESOURCE ds_1 IGNORE SINGLE TABLES;
```

- Drop the resource if it exists

```sql
DROP RESOURCE IF EXISTS ds_2;
```

### Reserved word

`DROP`, `RESOURCE`, `IF`, `EXISTS`, `IGNORE`, `SINGLE`, `TABLES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
