+++
title = "CREATE DB_DISCOVERY RULE"
weight = 2
+++

## Description

The `CREATE DB_DISCOVERY RULE` syntax is used to create a database discovery rule.

### Syntax

```sql
CreateDatabaseDiscoveryRule ::=
  'CREATE' 'DB_DISCOVERY' 'RULE' ( databaseDiscoveryDefinition | databaseDiscoveryConstruction ) ( ',' ( databaseDiscoveryDefinition | databaseDiscoveryConstruction ) )*

databaseDiscoveryDefinition ::=
    ruleName '(' 'RESOURCES' '(' resourceName ( ',' resourceName )* ')' ',' 'TYPE' '(' 'NAME' '=' typeName ( ',' 'PROPERTIES' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* )? ',' 'HEARTBEAT' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' ')' 
    
databaseDiscoveryConstruction ::=
    ruleName '(' 'RESOURCES' '(' resourceName ( ',' resourceName )* ')' ',' 'TYPE' '=' discoveryTypeName ',' 'HEARTBEAT' '=' discoveryHeartbeatName ')'
    
ruleName ::=
  identifier

resourceName ::=
  identifier

typeName ::=
  identifier

discoveryHeartbeatName ::=
  identifier
```

### Supplement

- `discoveryType` specifies the database discovery service type, `ShardingSphere` has built-in support for `MySQL.MGR`;
- Duplicate `ruleName` will not be created;
- The `discoveryType` and `discoveryHeartbeat` being used cannot be deleted;
- Names with `-` need to use `" "` when changing;
- When removing the `discoveryRule`, the `discoveryType` and `discoveryHeartbeat` used by the `discoveryRule` will not
  be removed.

### Example

#### When creating a `discoveryRule`, create both `discoveryType` and `discoveryHeartbeat`

```sql
CREATE
DB_DISCOVERY RULE db_discovery_group_0 (
    RESOURCES(ds_0, ds_1, ds_2),
    TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
    HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

#### Use the existing `discoveryType` and `discoveryHeartbeat` to create a `discoveryRule`

```sql
CREATE
DB_DISCOVERY RULE db_discovery_group_1 (
    RESOURCES(ds_0, ds_1, ds_2),
    TYPE=db_discovery_group_1_mgr,
    HEARTBEAT=db_discovery_group_1_heartbeat
);

```

### Reserved word

`CREATE`, `DB_DISCOVERY`, `RULE`, `RESOURCES`, `TYPE`, `NAME`, `PROPERTIES`, `HEARTBEAT`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
