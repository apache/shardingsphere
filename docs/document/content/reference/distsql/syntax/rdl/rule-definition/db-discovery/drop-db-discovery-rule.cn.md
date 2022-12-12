+++
title = "DROP DB_DISCOVERY RULE"
weight = 4
+++

## 描述

`DROP DB_DISCOVERY RULE` 语法用于为指定逻辑库删除数据库发现规则

### 语法定义

```sql
DropDatabaseDiscoveryRule ::=
  'DROP' 'DB_DISCOVERY' 'RULE'  dbDiscoveryRuleName (',' dbDiscoveryRuleName)*  ('FROM' databaseName)?

dbDiscoveryRuleName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定数据库删除多个数据库发现规则
 
```sql
DROP DB_DISCOVERY RULE group_0, group_1 FROM test1;
```

- 为当前数据库删除单个数据库发现规则

```sql
DROP DB_DISCOVERY RULE group_0;
```

### 保留字

`DROP`、`DB_DISCOVERY`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)