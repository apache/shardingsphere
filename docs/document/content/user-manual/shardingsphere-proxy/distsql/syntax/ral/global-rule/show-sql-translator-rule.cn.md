+++
title = "SHOW SQL_TRANSLATOR RULE"
weight = 5
+++

### 描述

`SHOW SQL_TRANSLATOR RULE` 语法用于查询 SQL 翻译器规则配置。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowSQLTranslatorRule ::=
  'SHOW' 'SQL_TRANSLATOR' 'RULE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 查询 SQL 翻译器规则

```sql
SHOW SQL_TRANSLATOR RULE;
```

### 保留字

`SHOW`、`SQL_TRANSLATOR`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
