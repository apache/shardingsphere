+++
title = "ADD SHARDING HINT TABLE_VALUE"
weight = 5
+++

### 描述

`ADD SHARDING HINT TABLE_VALUE` 语法用于针对当前连接，为指定表添加表分片值
### 语法

```sql
AddShardingHintDatabaseValue ::=
  'ADD' 'SHARDING' 'HINT' 'TABLE_VALUE' shardingHintTableValueDefination
  
  shardingHintTableValueDefination ::=
    tableName '=' tableShardingValue

  tableName ::=
    identifier

  tableShardingValue ::=
    int
```

### 示例

- 为指定表添加表分片值

```sql
ADD SHARDING HINT TABLE_VALUE t_order = 100;
```

### 保留字

`ADD`、`SHARDING`、`HINT`、`TABLE_VALUE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)