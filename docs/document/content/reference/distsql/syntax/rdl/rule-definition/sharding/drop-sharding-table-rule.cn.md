+++
title = "DROP SHARDING TABLE RULE"
weight = 4
+++

## 描述

`DROP SHARDING TABLE RULE` 语法用于删除指定逻辑库的指定分片规则

### 语法定义

```sql
DropShardingTableRule ::=
  'DROP' 'SHARDING' 'TABLE' 'RULE'  shardingRuleName (',' shardingRuleName)*  ('FROM' databaseName)?

shardingRuleName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定逻辑库删除多个指定分片规则
 
```sql
DROP SHARDING TABLE RULE t_order, t_order_item FROM test1;
```

- 为当前逻辑库删除单个指定分片规则

```sql
DROP SHARDING TABLE RULE t_order;
```

### 保留字

`DROP`、`SHARDING`、`TABLE`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)