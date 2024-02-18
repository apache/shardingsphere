+++
title = "ALTER DEFAULT SHADOW ALGORITHM"
weight = 5
+++

## 描述

`ALTER DEFAULT SHADOW ALGORITHM` 语法用于修改影子库默认算法规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterDefaultShadowAlgorithm ::=
  'ALTER' 'DEFAULT' 'SHADOW' 'ALGORITHM' shadowAlgorithm 

shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' algorithmType ',' propertiesDefiinition ')'
    
algorithmType ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `algorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SQL_HINT`。

### 示例

- 修改默认影子库压测算法

```sql
ALTER DEFAULT SHADOW ALGORITHM TYPE(NAME="SQL_HINT");
```

### 保留字

`ALTER`、`DEFAULT`、`SHADOW`、`ALGORITHM`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
