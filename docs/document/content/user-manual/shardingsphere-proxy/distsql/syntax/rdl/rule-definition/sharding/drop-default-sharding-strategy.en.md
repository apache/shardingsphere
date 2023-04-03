+++
title = "DROP DEFAULT SHARDING STRATEGY"
weight = 6
+++

## Description

The `DROP DEFAULT SHARDING STRATEGY` syntax is used to drop default sharding strategy for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropDefaultShardingStrategy ::=
  'DROP' 'DEFAULT' 'SHARDING' ('TABLE' | 'DATABASE') 'STRATEGY' ifExists? ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted;
- `ifExists` clause is used for avoid `Default sharding strategy not exists` error.

### Example

- Drop default sharding table strategy for specified database

```sql
DROP DEFAULT SHARDING TABLE STRATEGY FROM sharding_db;
```

- Drop default sharding database strategy for current database

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY;
```

- Drop default sharding table strategy with `ifExists` clause

```sql
DROP DEFAULT SHARDING TABLE STRATEGY IF EXISTS;
```

- Drop default sharding database strategy with `ifExists` clause

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY IF EXISTS;
```

### Reserved word

`DROP`, `DEFAULT` , `SHARDING`, `TABLE`, `DATABASE` ,`STRATEGY`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
