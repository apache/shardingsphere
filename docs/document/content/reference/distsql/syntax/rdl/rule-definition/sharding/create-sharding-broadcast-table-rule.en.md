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
- Broadcast table rules cannot be created repeatedly, but can contain multiple broadcast tables

### Example

#### Create sharding broadcast table rule

```SQL
-- Add t_province, t_city to broadcast table rules
CREATE SHARDING BROADCAST TABLE RULES (t_province, t_city);
```


