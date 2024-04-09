+++
title = "CREATE DEFAULT SHADOW ALGORITHM"
weight = 4
+++

## 描述

`CREATE DEFAULT SHADOW ALGORITHM` 语法用于创建影子库默认算法规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateDefaultShadowAlgorithm ::=
  'CREATE' 'DEFAULT' 'SHADOW' 'ALGORITHM' ifNotExists? shadowAlgorithm 

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

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

- `algorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SQL_HINT`；
- `ifNotExists` 子句用于避免出现 `Duplicate default shadow algorithm` 错误。

### 示例

- 创建默认影子库压测算法

```sql
CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME="SQL_HINT");
```

- 使用 `ifNotExists` 子句创建默认影子库压测算法

```sql
CREATE DEFAULT SHADOW ALGORITHM IF NOT EXISTS TYPE(NAME="SQL_HINT");
```

### 保留字

`CREATE`、`DEFAULT`、`SHADOW`、`ALGORITHM`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
