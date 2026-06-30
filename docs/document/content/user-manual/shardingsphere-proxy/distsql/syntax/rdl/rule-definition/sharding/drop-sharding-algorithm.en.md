+++
title = "DROP SHARDING ALGORITHM"
weight = 11
+++

## Description

The `DROP SHARDING ALGORITHM` syntax is used to drop sharding algorithms from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingAlgorithm ::=
  'DROP' 'SHARDING' 'ALGORITHM' ifExists? algorithmName (',' algorithmName)*

ifExists ::=
  'IF' 'EXISTS'

algorithmName ::=
  identifier

```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `ifExists` clause used for avoid `Sharding algorithm not exists` error.

### Example

- Drop sharding algorithm

```sql
DROP SHARDING ALGORITHM t_order_hash_mod;
```

- Drop multiple sharding algorithms

```sql
DROP SHARDING ALGORITHM t_order_hash_mod, t_item_hash_mod;
```

- Drop sharding algorithm with `ifExists` clause

```sql
DROP SHARDING ALGORITHM IF EXISTS t_order_hash_mod;
```

### Reserved word

`DROP`, `SHARDING`, `ALGORITHM`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
