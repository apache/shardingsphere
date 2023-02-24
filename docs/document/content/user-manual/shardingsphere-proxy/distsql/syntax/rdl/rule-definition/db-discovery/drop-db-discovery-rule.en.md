+++
title = "DROP DB_DISCOVERY RULE"
weight = 4
+++

## Description

The `DROP DB_DISCOVERY RULE` syntax is used to drop database discovery rule for specified database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropDatabaseDiscoveryRule ::=
  'DROP' 'DB_DISCOVERY' 'RULE' ifExists? dbDiscoveryRuleName (',' dbDiscoveryRuleName)* ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

dbDiscoveryRuleName ::=
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
- `ifExists` clause is used for avoid `Database discovery rule not exists` error.

### Example

- Drop mutiple database discovery rule for specified database

```sql
DROP DB_DISCOVERY RULE group_0, group_1 FROM discovery_db;
```

- Drop single database discovery rule for current database

```sql
DROP DB_DISCOVERY RULE group_0;
```

- Drop database discovery rule with `ifExists` clause

```sql
DROP DB_DISCOVERY RULE IF EXISTS group_0;
```

### Reserved word

`DROP`, `DB_DISCOVERY`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
