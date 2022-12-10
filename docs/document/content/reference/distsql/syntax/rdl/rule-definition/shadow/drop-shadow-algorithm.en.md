+++
title = "DROP SHADOW ALGORITHM"
weight = 8
+++

## Description

The `DROP SHADOW ALGORITHM` syntax is used to drop shadow algorithm for specified database

### Syntax

```sql
DropShadowAlgorithm ::=
  'DROP' 'SHADOW' 'ALGORITHM' shadowAlgorithmName(',' shadowAlgorithmName)* ('FROM' databaseName)?

shadowAlgorithmName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Example

- Drop mutiple shadow algorithm for specified database

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_simple_hint_0, shadow_rule_t_order_item_simple_hint_0 FROM test1;
```

- Drop single shadow algorithm for current database

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_simple_hint_0;
```

### Reserved word

`DROP`, `SHODOW`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
