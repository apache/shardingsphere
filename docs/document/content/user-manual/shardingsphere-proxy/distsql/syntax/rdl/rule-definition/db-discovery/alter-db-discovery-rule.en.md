+++
title = "ALTER DB_DISCOVERY RULE"
weight = 3
+++

## Description

The `ALTER DB_DISCOVERY RULE` syntax is used to alter a database discovery rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterDatabaseDiscoveryRule ::=
  'ALTER' 'DB_DISCOVERY' 'RULE' databaseDiscoveryDefinition (',' databaseDiscoveryDefinition)*

databaseDiscoveryDefinition ::=
  ruleName '(' 'STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')' ',' 'TYPE' '(' 'NAME' '=' typeName (',' propertiesDefinition)? ')' ',' 'HEARTBEAT' '(' propertiesDefinition ')' ')' 

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

ruleName ::=
  identifier

storageUnitName ::=
  identifier

typeName ::=
  identifier

discoveryHeartbeatName ::=
  identifier

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `discoveryType` specifies the database discovery service type, `ShardingSphere` has built-in support for `MySQL.MGR`;

### Example

- Alter database discovery rule

```sql
ALTER DB_DISCOVERY RULE db_discovery_group_0 (
    STORAGE_UNITS(ds_0, ds_1, ds_2),
    TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
    HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

### Reserved word

`ALTER`, `DB_DISCOVERY`, `RULE`, `STORAGE_UNITS`, `TYPE`, `NAME`, `PROPERTIES`, `HEARTBEAT`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
