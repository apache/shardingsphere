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
  'DROP' 'DB_DISCOVERY' 'TYPE'  dbDiscoveryTypeName (',' dbDiscoveryTypeName)* ('FROM' databaseName)?

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

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, No database selected will be prompted.

- `dbDiscoveryTypeName` obtain through [SHOW DB_DISCOVERY TYPE](/en/reference/distsql/syntax/rql/rule-query/db-discovery/show-db-discovery-type/) syntax query.

### Example

- Drop mutiple database discovery type for specified database

```sql
DROP DB_DISCOVERY TYPE group_0_mysql_mgr, group_1_mysql_mgr FROM test1;
```

- Drop single database discovery type for current database

```sql
DROP DB_DISCOVERY TYPE group_0_mysql_mgr, group_1_mysql_mgr;
```

### Reserved word

`DROP`, `DB_DISCOVERY`, `TYPE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
