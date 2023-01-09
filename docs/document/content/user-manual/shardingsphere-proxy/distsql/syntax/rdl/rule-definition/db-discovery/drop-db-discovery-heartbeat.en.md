+++
title = "DROP DB_DISCOVERY HEARTBEAT"
weight = 5
+++

## Description

The `DROP DB_DISCOVERY HEARTBEAT` syntax is used to drop database discovery heartbeat for specified database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropDatabaseDiscoveryHeartbeat ::=
  'DROP' 'DB_DISCOVERY' 'HEARTBEAT' ifExists? dbDiscoveryHeartbeatName (',' dbDiscoveryHeartbeatName)*  ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

dbDiscoveryHeartbeatName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted;
- `ifExists` clause is used for avoid `Database discovery heartbeat not exists` error.

### Example

- Drop mutiple database discovery heartbeat for specified database

```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat, group_1_heartbeat FROM discovery_db;
```

- Drop single database discovery heartbeat for current database

```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat;
```

- Drop database discovery heartbeat with `ifExists` clause

```sql
DROP DB_DISCOVERY HEARTBEAT IF EXISTS group_0_heartbeat;
```

### Reserved word

`DROP`, `DB_DISCOVERY`, `HEARTBEAT`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
