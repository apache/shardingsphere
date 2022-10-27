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
    ruleName '(' 'STORAGE_UNITS' '(' storageUnit ( ',' storageUnit )* ')' ',' 'TYPE' '(' 'NAME' '=' typeName ( ',' 'PROPERTIES' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* )? ',' 'HEARTBEAT' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' ')' 
    
databaseDiscoveryConstruction ::=
    ruleName '(' 'STORAGE_UNITS' '(' storageUnit ( ',' storageUnit )* ')' ',' 'TYPE' '=' discoveryTypeName ',' 'HEARTBEAT' '=' discoveryHeartbeatName ')'
    
ruleName ::=
  identifier

storageUnit ::=
  identifier

typeName ::=
  identifier

discoveryHeartbeatName ::=
  identifier
```

### Supplement

- `discoveryType` specifies the database discovery service type, `ShardingSphere` has built-in support for `MySQL.MGR`;
- Duplicate `ruleName` will not be created.

### Example

#### When creating a `discoveryRule`, create both `discoveryType` and `discoveryHeartbeat`

```sql
CREATE
DB_DISCOVERY RULE db_discovery_group_0 (
    STORAGE_UNITS(ds_0, ds_1, ds_2),
    TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
    HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

#### Use the existing `discoveryType` and `discoveryHeartbeat` to create a `discoveryRule`

```sql
CREATE
DB_DISCOVERY RULE db_discovery_group_1 (
    STORAGE_UNITS(ds_0, ds_1, ds_2),
    TYPE=db_discovery_group_1_mgr,
    HEARTBEAT=db_discovery_group_1_heartbeat
);

```

### Reserved word

`CREATE`, `DB_DISCOVERY`, `RULE`, `STORAGE_UNITS`, `TYPE`, `NAME`, `PROPERTIES`, `HEARTBEAT`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
