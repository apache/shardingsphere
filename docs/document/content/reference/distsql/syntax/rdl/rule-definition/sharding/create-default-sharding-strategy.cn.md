+++
title = "CREATE DEFAULT SHARDING STRATEGY"
weight = 5
+++

## 描述

`CREATE DEFAULT SHARDING STRATEGY` 语法用于创建默认的分片策略

### 语法定义

```sql
CreateDefaultShardingStrategy ::=
  'CREATE' 'DEFAULT' 'SHARDING' ('DATABASE' | 'TABLE') 'STRATEGY' '(' shardingStrategy ')'

shardingStrategy ::=
  'TYPE' '=' strategyType ',' ( 'SHARDING_COLUMN' '=' columnName  | 'SHARDING_COLUMNS' '=' columnNames ) ',' ( 'SHARDING_ALGORITHM' '=' algorithmName | algorithmDefinition )

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

columnNames ::=
  columnName (',' columnName)+

columnName ::=
  string

algorithmName ::=
  string
  
algorithmType ::=
  string
```

### 补充说明

- 当使用复合分片算法时，需要通过 `SHARDING_COLUMNS` 指定多个分片键；
- `algorithmType` 为分片算法类型，详细的分片算法类型信息请参考[分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/)。

### 示例

#### 1.通过已有的分片算法创建默认分库策略

```sql
-- 创建分片算法
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
);

-- 创建默认分库策略
CREATE DEFAULT SHARDING DATABASE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM=database_inline
);
```

#### 2.同时创建分片算法和默认分表策略

```sql
-- 创建默认分表策略
CREATE DEFAULT SHARDING TABLE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME=inline, PROPERTIES("algorithm-expression"="t_order_${user_id % 2}")))
);
```

### 保留字

`CREATE`、`DEFAULT`、`SHARDING`、`DATABASE`、`TABLE`、`STRATEGY`、`TYPE`、`SHARDING_COLUMN`、`SHARDING_COLUMNS`、`SHARDING_ALGORITHM`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING ALGORITHM](/cn/reference/distsql/syntax/rdl/rule-definition/create-sharding-algorithm/)
