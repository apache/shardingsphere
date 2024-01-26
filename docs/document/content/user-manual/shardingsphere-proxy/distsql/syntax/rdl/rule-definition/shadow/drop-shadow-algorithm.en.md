+++
title = "DROP SHADOW ALGORITHM"
weight = 7
+++

## Description

The `DROP SHADOW ALGORITHM` syntax is used to drop shadow algorithm for specified database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShadowAlgorithm ::=
  'DROP' 'SHADOW' 'ALGORITHM' ifExists? algorithmName (',' algorithmName)* ('FROM' databaseName)?

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

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted;
- `ifExists` clause is used for avoid `shadow algorithm not exists` error.

### Example

- Drop mutiple shadow algorithm for specified database

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_sql_hint_0, shadow_rule_t_order_item_sql_hint_0 FROM shadow_db;
```

- Drop single shadow algorithm for current database

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_sql_hint_0;
```

- Drop shadow algorithm with `ifExists` clause

```sql
DROP SHADOW ALGORITHM IF EXISTS shadow_rule_t_order_sql_hint_0;
```

### Reserved word

`DROP`, `SHODOW`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
