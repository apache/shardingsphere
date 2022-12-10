+++
title = "CREATE BROADCAST TABLE RULE"
weight = 16
+++

## Description

The `CREATE BROADCAST TABLE RULE` syntax is used to create broadcast table rules for tables that need to be
broadcast (broadcast tables)

### Syntax

```sql
CreateBroadcastTableRule ::=
  'CREATE' 'BROADCAST' 'TABLE' 'RULE'  tableName (',' tableName)* 

tableName ::=
  identifier
```

### Supplement

- `tableName` can use an existing table or a table that will be created;
- Only one broadcast rule can exist, but can contain multiple broadcast tables, so can not
  execute `CREATE BROADCAST TABLE RULE` more than one time. 

### Example

#### Create broadcast table rule

```sql
-- Add t_province, t_city to broadcast table rules
CREATE BROADCAST TABLE RULE t_province, t_city;
```

### Reserved word

`CREATE`, `BROADCAST`, `TABLE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
