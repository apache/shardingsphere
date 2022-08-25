+++
title = "CREATE SHARDING ALGORITHM"
weight = 4
+++

## 描述

`CREATE SHARDING ALGORITHM` 语法用于为当前所选的逻辑库添加分片算法

### 语法定义

```sql
CreateShardingAlgorithm ::=
  'CREATE' 'SHARDING' 'ALGORITHM' shardingAlgorithmName '(' algorithmDefinition ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )*

shardingAlgorithmName ::=
  identifier
  
algorithmType ::=
  identifier
```

### 补充说明

- `algorithmType` 为分片算法类型，详细的分片算法类型信息请参考[分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/)。

### 示例

#### 1.创建分片算法

```SQL
-- 创建类型为 INLINE 的分片算法
CREATE SHARDING ALGORITHM inline_algorithm (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
);

-- 创建类型为 AUTO_INTERVAL 的分片算法
CREATE SHARDING ALGORITHM interval_algorithm (
    TYPE(NAME="auto_interval", PROPERTIES("datetime-lower"="2022-01-01 00:00:00", "datetime-upper"="2022-01-03 00:00:00", "sharding-seconds"="86400"))
);
```

### 保留字

`CREATE`、`SHARDING`、`ALGORITHM`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
