+++
title = "DROP SHARDING KEY GENERATOR"
weight = 10
+++

## Description

The `DROP SHARDING KEY GENERATOR` syntax is used to drop sharding key generators from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingKeyGenerator ::=
  'DROP' 'SHARDING' 'KEY' 'GENERATOR' ifExists? keyGeneratorName (',' keyGeneratorName)*

ifExists ::=
  'IF' 'EXISTS'

keyGeneratorName ::=
  identifier

```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `ifExists` clause is used to avoid `Sharding key generator not exists` error.

### Example

- Drop sharding key generator

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake;
```

- Drop multiple sharding key generators

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake, t_item_snowflake;
```

- Drop sharding key generator with `ifExists` clause

```sql
DROP SHARDING KEY GENERATOR IF EXISTS t_order_snowflake;
```

### Reserved word

`DROP`, `SHARDING`, `KEY`, `GENERATOR`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
