+++
title = "DROP TRAFFIC RULE"
weight = 10
+++

### 描述

`DROP TRAFFIC RULE` 语法用于删除指定的双路由规则
### 语法

```sql
DropTrafficRule ::=
  'DROP' 'TRAFFIC' 'RULE' ruleName (',' ruleName)?

ruleName ::=
  identifier
```

### 示例

- 删除指定双路由规则

```sql
DROP TRAFFIC RULE sql_match_traffic;
```

- 删除多个双路由规则

```sql
DROP TRAFFIC RULE sql_match_traffic, sql_hint_traffic;
```

### 保留字

`DROP`、`TRAFFIC`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)