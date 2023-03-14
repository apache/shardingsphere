+++
title = "DROP DB_DISCOVERY TYPE"
weight = 6
+++

## Description

The `DROP DB_DISCOVERY TYPE` syntax is used to drop database discovery type for specified database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropDatabaseDiscoveryType ::=
  'DROP' 'DB_DISCOVERY' 'TYPE' ifExists? dbDiscoveryTypeName (',' dbDiscoveryTypeName)* ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

dbDiscoveryTypeName ::=
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

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, No database selected will be prompted;
- `dbDiscoveryTypeName` obtain through [SHOW DB_DISCOVERY TYPE](/en/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/db-discovery/show-db-discovery-type/) syntax query;
- `ifExists` clause is used for avoid `Database discovery type not exists` error.

### Example

- Drop mutiple database discovery type for specified database

```sql
DROP DB_DISCOVERY TYPE group_0_mysql_mgr, group_1_mysql_mgr FROM discovery_db;
```

- Drop single database discovery type for current database

```sql
DROP DB_DISCOVERY TYPE group_0_mysql_mgr;
```

- Drop database discovery type with `ifExists` clause

```sql
DROP DB_DISCOVERY TYPE IF EXISTS group_0_mysql_mgr;
```

### Reserved word

`DROP`, `DB_DISCOVERY`, `TYPE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
