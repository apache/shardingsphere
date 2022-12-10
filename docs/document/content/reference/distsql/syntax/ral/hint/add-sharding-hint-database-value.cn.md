+++
title = "ADD SHARDING HINT DATABASE_VALUE"
weight = 4
+++

### 描述

`ADD SHARDING HINT DATABASE_VALUE` 语法用于针对当前连接，为指定表添加数据库分片值
### 语法

```sql
AddShardingHintDatabaseValue ::=
  'ADD' 'SHARDING' 'HINT' 'DATABASE_VALUE' shardingHintDatabaseValueDefination
  
  shardingHintDatabaseValueDefination ::=
    tableName '=' databaseShardingValue

  tableName ::=
    identifier

  databaseShardingValue ::=
    int
```

### 示例

- 为指定表添加数据库分片值

```sql
ADD SHARDING HINT DATABASE_VALUE t_order = 100;
```

### 保留字

`ADD`、`SHARDING`、`HINT`、`DATABASE_VALUE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)