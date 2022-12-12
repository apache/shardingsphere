+++
title = "DROP SHADOW RULE"
weight = 4
+++

## 描述

`DROP SHADOW RULE` 语法用于为指定逻辑库删除影子库压测规则

### 语法定义

```sql
DropShadowRule ::=
  'DROP' 'SHADOW' 'TABLE' 'RULE' shadowRuleName ('FROM' databaseName)?

shadowRuleName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定数据库删除影子库压测规则
 
```sql
DROP SHADOW RULE shadow_rule FROM test1;
```

- 为当前数据库删除影子库压测规则

```sql
DROP SHADOW RULE shadow_rule;
```

### 保留字

`DROP`、`SHADOW`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)