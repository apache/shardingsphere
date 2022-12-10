+++
title = "DROP DEFAULT SHADOW ALGORITHM"
weight = 7
+++

## 描述

`DROP DEFAULT SHADOW ALGORITHM` 语法用于为指定逻辑库删除默认影子库压测算法

### 语法定义

```sql
DropDefaultShadowAlgorithm ::=
  'DROP' 'DEFAULT' 'SHADOW' 'ALGORITHM' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定数据库删除默认影子库压测算法

```sql
DROP DEFAULT SHADOW ALGORITHM FROM test1;
```

- 为当前数据库删除默认影子库压测算法

```sql
DROP DEFAULT SHADOW ALGORITHM;
```

### 保留字

`DROP`、`DEFAULT`、`SHADOW`、`ALGORITHM`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)