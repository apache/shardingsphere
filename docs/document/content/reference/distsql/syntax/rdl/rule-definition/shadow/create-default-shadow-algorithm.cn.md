+++
title = "CREATE DEFAULT SHADOW ALGORITHM"
weight = 5
+++

## 描述

`CREATE DEFAULT SHADOW ALGORITHM` 语法用于创建影子库默认算法规则。

### 语法定义

```sql
CreateDefaultShadowAlgorithm ::=
  'CREATE' 'DEFAULT' 'SHADOW' 'ALGORITHM' shadowAlgorithm 

shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' shadowAlgorithmType ',' 'PROPERTIES' '(' ( 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ) ')' ')'
    
shadowAlgorithmType ::=
  string
```

### 补充说明

- `shadowAlgorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SIMPLE_HINT`。

### 示例

- 创建默认影子库压测算法

```sql
CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar");
```

### 保留字

`CREATE`、`DEFAULT`、`SHADOW`、`ALGORITHM`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
