+++
title = "CREATE DB_DISCOVERY HEARTBEAT"
weight = 4
+++

## Description

The `CREATE DB_DISCOVERY HEARTBEAT` syntax is used to create a database discovery heartbeat rule.

### Syntax

```sql
CreateDatabaseDiscoveryHeartbeat ::=
  'CREATE' 'DB_DISCOVERY' 'HEARTBEAT' databaseDiscoveryHeartbaetDefinition ( ',' databaseDiscoveryHeartbaetDefinition )*

databaseDiscoveryHeartbaetDefinition ::=
    discoveryHeartbeatName '(' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' ')'
    
discoveryHeartbeatName ::=
  identifier
```

### Supplement

- Names with `-` need to use `" "` when changing.

### Example

#### Create `HEARTBEAT`

```sql
CREATE DB_DISCOVERY HEARTBEAT db_discovery_group_1_heartbeat(
  PROPERTIES('keep-alive-cron'='0/5 * * * * ?')
);
```

### Reserved word

`CREATE`, `DB_DISCOVERY`, `HEARTBEAT`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
