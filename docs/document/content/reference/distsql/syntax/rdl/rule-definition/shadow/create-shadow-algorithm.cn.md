+++
title = "CREATE SHADOW ALGORITHM"
weight = 3
+++

## 描述

`CREATE SHADOW ALGORITHM` 语法用于创建影子库算法规则。

### 语法定义

```sql
CreateShadowAlgorithm ::=
  'CREATE' 'SHADOW' 'RULE' shadowAlgorithm ( ',' shadowAlgorithm )*

shadowAlgorithm ::=
  '(' ( algorithmName ',' )? 'TYPE' '(' 'NAME' '=' shadowAlgorithmType ',' 'PROPERTIES' '(' ( 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ) ')' ')'
    
algorithmName ::=
  identifier

shadowAlgorithmType ::=
  string
```

### 补充说明

- `shadowAlgorithm` 可同时作用于多个 `shadowTableRule`；
- `algorithmName` 未指定时会根据 `ruleName`、`tableName` 和 `shadowAlgorithmType` 自动生成；
- `shadowAlgorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SIMPLE_HINT`。

### 示例

#### 创建影子库压测算法

```sql
CREATE SHADOW ALGORITHM 
  (simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"))), 
  (user_id_match_algorithm, TYPE(NAME="REGEX_MATCH",PROPERTIES("operation"="insert", "column"="user_id", "regex"='[1]'))
);
```

### 保留字

`CREATE`、`SHADOW`、`ALGORITHM`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
