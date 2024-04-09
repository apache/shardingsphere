+++
title = "DROP SHARDING ALGORITHM"
weight = 11
+++

## Description

The `DROP SHARDING ALGORITHM` syntax is used to drop sharding algorithm for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingAlgorithm ::=
  'DROP' 'SHARDING' 'ALGORITHM' algorithmName ifExists? ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

algorithmName ::=
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

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted;
- `ifExists` clause used for avoid `Sharding algorithm not exists` error.

### Example

- Drop sharding algorithm for specified database

```sql
DROP SHARDING ALGORITHM t_order_hash_mod FROM sharding_db;
```

- Drop sharding algorithm for current database

```sql
DROP SHARDING ALGORITHM t_order_hash_mod;
```

- Drop sharding algorithm with `ifExists` clause

```sql
DROP SHARDING ALGORITHM IF EXISTS t_order_hash_mod;
```

### Reserved word

`DROP`, `SHARDING`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
