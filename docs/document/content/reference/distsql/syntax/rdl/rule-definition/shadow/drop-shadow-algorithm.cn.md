+++
title = "DROP SHADOW ALGORITHM"
weight = 8
+++

## 描述

`DROP SHADOW ALGORITHM` 语法用于为指定逻辑库删除影子库压测算法

### 语法定义

```sql
DropShadowAlgorithm ::=
  'DROP' 'SHADOW' 'ALGORITHM' shadowAlgorithmName(',' shadowAlgorithmName)* ('FROM' databaseName)?

shadowAlgorithmName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定数据库删除多个影子库压测算法
 
```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_simple_hint_0, shadow_rule_t_order_item_simple_hint_0 FROM test1;
```

- 为当前数据库删除单个影子库压测算法

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_simple_hint_0;
```

### 保留字

`DROP`、`SHADOW`、`ALGORITHM`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)