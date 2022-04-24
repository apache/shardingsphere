+++
title = "CREATE SHARDING BROADCAST TABLE RULE"
weight = 5
+++

## Description

The `CREATE SHARDING BROADCAST TABLE RULE` syntax is used to create broadcast table rules for tables that need to be broadcast (broadcast tables)

### Syntax

```SQL
CreateBroadcastTableRule ::=
  'CREATE' 'SHARDING' 'BROADCAST' 'TABLE' 'RULES' '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### Supplement

- `tableName` can use an existing table or a table that will be created
- Only one broadcast rule can exist, but can contain multiple broadcast tables, so can not execute `CREATE SHARDING BROADCAST TABLE RULE` more than one time.  When the broadcast table rule already exists but the broadcast table needs to be added, you need to use `ALTER BROADCAST TABLE RULE` to modify the broadcast table rule

### Example

#### Create sharding broadcast table rule

```SQL
-- Add t_province, t_city to broadcast table rules
CREATE SHARDING BROADCAST TABLE RULES (t_province, t_city);
```


