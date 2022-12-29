+++
title = "CREATE DEFAULT SHADOW ALGORITHM"
weight = 5
+++

## 描述

`CREATE DEFAULT SHADOW ALGORITHM` 语法用于创建影子库默认算法规则。

### 语法定义

```sql
CreateDefaultShadowAlgorithm ::=
  'CREATE' 'DEFAULT' 'SHADOW' 'ALGORITHM' ifNotExists? shadowAlgorithm 

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' shadowAlgorithmType ',' propertiesDefiinition ')'
    
shadowAlgorithmType ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal
```

### 补充说明

- `shadowAlgorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SIMPLE_HINT`；
- `ifNotExists` 子句用于避免出现 `Duplicate default shadow algorithm` 错误。

### 示例

- 创建默认影子库压测算法

```sql
CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"));
```

- 使用 `ifNotExists` 子句创建默认影子库压测算法

```sql
CREATE DEFAULT SHADOW ALGORITHM IF NOT EXISTS TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"));
```

### 保留字

`CREATE`、`DEFAULT`、`SHADOW`、`ALGORITHM`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
