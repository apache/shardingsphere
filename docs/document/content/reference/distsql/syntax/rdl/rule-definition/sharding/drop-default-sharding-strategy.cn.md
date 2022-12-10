+++
title = "DROP DEFAULT SHARDING STRATEGY"
weight = 7
+++

## 描述

`DROP DEFAULT SHARDING STRATEGY` 语法用于删除指定逻辑库的默认分片策略

### 语法定义

```sql
DropDefaultShardingStrategy ::=
  'DROP' 'DEFAULT' 'SHARDING' ('TABLE' | 'DATABASE') 'STRATEGY' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定逻辑库删除默认表分片策略
 
```sql
DROP DEFAULT SHARDING TABLE STRATEGY FROM test1;
```

- 为当前逻辑库删除默认库分片策略

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY;
```

### 保留字

`DROP`、`DEFAULT`、`SHARDING`、`TABLE`、`DATABASE`、`STRATEGY`、`FROM`
### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)