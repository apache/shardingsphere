+++
title = "DROP READWRITE_SPLITTING RULE"
weight = 4
+++

## 描述

`DROP READWRITE_SPLITTING RULE` 语法用于为指定逻辑库删除读写分离

### 语法定义

```sql
DropReadwriteSplittingRule ::=
  'DROP' 'READWRITE_SPLITTING' 'RULE' ('FROM' databaseName)

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定逻辑库删除读写分离规则
 
```sql
DROP READWRITE_SPLITTING RULE ms_group_1 FROM test1;
```

- 为当前逻辑库删除读写分离规则

```sql
DROP READWRITE_SPLITTING RULE ms_group_1;
```

### 保留字

`DROP`、`READWRITE_SPLITTING`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)