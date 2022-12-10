+++
title = "DROP DB_DISCOVERY HEARTBEAT"
weight = 5
+++

## Description

The `DROP DB_DISCOVERY HEARTBEAT` syntax is used to drop database discovery heartbeat for specified database

### Syntax

```sql
DropDatabaseDiscoveryHeartbeat ::=
  'DROP' 'DB_DISCOVERY' 'HEARTBEAT'  dbDiscoveryHeartbeatName (',' dbDiscoveryHeartbeatName)*  ('FROM' databaseName)?

dbDiscoveryHeartbeatName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Example

- Drop mutiple database discovery heartbeat for specified database

```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat, group_1_heartbeat FROM test1;
```

- Drop single database discovery heartbeat for current database

```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat;
```

### Reserved word

`DROP`, `DB_DISCOVERY`, `HEARTBEAT`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
