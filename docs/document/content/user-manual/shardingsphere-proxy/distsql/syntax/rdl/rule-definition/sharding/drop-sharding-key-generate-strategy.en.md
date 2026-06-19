+++
title = "DROP SHARDING KEY GENERATE STRATEGY"
weight = 9
+++

## Description

The `DROP SHARDING KEY GENERATE STRATEGY` syntax is used to drop specified sharding key generate strategies in the currently selected database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingKeyGenerateStrategy ::=
  'DROP' 'SHARDING' 'KEY' 'GENERATE' 'STRATEGY' ifExists? keyGenerateStrategyName (',' keyGenerateStrategyName)*

ifExists ::=
  'IF' 'EXISTS'

keyGenerateStrategyName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- This syntax only removes sharding key generate strategy definitions, and does not cascade to remove the referenced sharding key generators;
- `ifExists` clause is used to avoid `Sharding key generate strategy not exists` error.

### Example

- Drop sharding key generate strategy

```sql
DROP SHARDING KEY GENERATE STRATEGY order_id_strategy;
```

- Drop multiple sharding key generate strategies at once

```sql
DROP SHARDING KEY GENERATE STRATEGY order_id_strategy, order_sequence_strategy;
```

- Drop sharding key generate strategy with `ifExists` clause

```sql
DROP SHARDING KEY GENERATE STRATEGY IF EXISTS order_id_strategy;
```

### Reserved word

`DROP`, `SHARDING`, `KEY`, `GENERATE`, `STRATEGY`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW SHARDING KEY GENERATE STRATEGY](/en/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generate-strategy/)
