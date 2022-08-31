+++
title = "CREATE DB_DISCOVERY TYPE"
weight = 3
+++

## Description

The `CREATE DB_DISCOVERY TYPE` syntax is used to create a database discovery type rule.

### Syntax

```sql
CreateDatabaseDiscoveryType ::=
  'CREATE' 'DB_DISCOVERY' 'TYPE' databaseDiscoveryTypeDefinition ( ',' databaseDiscoveryTypeDefinition )*

databaseDiscoveryTypeDefinition ::=
    discoveryTypeName '(' 'TYPE' '(' 'NAME' '=' typeName ( ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' )? ')' ')'
    
discoveryTypeName ::=
  string

typeName ::=
  string
```

### Supplement

- `discoveryType` specifies the database discovery service type, `ShardingSphere` has built-in support for `MySQL.MGR`.

### Example

#### Create `discoveryType`

```sql
CREATE DB_DISCOVERY TYPE db_discovery_group_1_mgr(
  TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec'))
);
```

### Reserved word

`CREATE`, `DB_DISCOVERY`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
