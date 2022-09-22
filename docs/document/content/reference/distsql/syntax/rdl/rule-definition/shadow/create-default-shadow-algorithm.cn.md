+++
title = "CREATE DEFAULT SHADOW ALGORITHM"
weight = 4
+++

## 描述

`CREATE DEFAULT SHADOW ALGORITHM` 语法用于创建影子库默认算法规则。

### 语法定义

```sql
CreateDefaultShadowAlgorithm ::=
  'CREATE' 'DEFAULT' 'SHADOW' 'ALGORITHM' 'NAME' '=' algorithmName
    
algorithmName ::=
  identifier
```

### 示例

#### 创建影子库压测算法

```sql
CREATE DEFAULT SHADOW ALGORITHM NAME = simple_hint_algorithm;
```

### 保留字

`CREATE`、`DEFAULT`、`SHADOW`、`ALGORITHM`、`NAME`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
