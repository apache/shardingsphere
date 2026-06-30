+++
title = "DROP SHADOW ALGORITHM"
weight = 7
+++

## Description

The `DROP SHADOW ALGORITHM` syntax is used to drop shadow algorithms from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShadowAlgorithm ::=
  'DROP' 'SHADOW' 'ALGORITHM' ifExists? algorithmName (',' algorithmName)*

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

- `ifExists` clause is used for avoid `shadow algorithm not exists` error.

### Example

- Drop multiple shadow algorithms

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_sql_hint_0, shadow_rule_t_order_item_sql_hint_0;
```

- Drop a shadow algorithm

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_sql_hint_0;
```

- Drop shadow algorithm with `ifExists` clause

```sql
DROP SHADOW ALGORITHM IF EXISTS shadow_rule_t_order_sql_hint_0;
```

### Reserved word

`DROP`, `SHADOW`, `ALGORITHM`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
