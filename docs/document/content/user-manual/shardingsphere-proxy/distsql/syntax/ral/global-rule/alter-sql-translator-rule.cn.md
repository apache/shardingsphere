+++
title = "ALTER SQL_TRANSLATOR RULE"
weight = 6
+++

### 描述

`ALTER SQL_TRANSLATOR RULE` 语法用于修改 SQL 翻译器规则配置。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterSQLTranslatorRule ::=
  'ALTER' 'SQL_TRANSLATOR' 'RULE' '(' sqlTranslatorRuleDefinition ')'

sqlTranslatorRuleDefinition ::=
  algorithmDefinition (',' useOriginalSQLWhenTranslatingFailedClause)?

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' translatorType (',' 'PROPERTIES' '(' propertiesDefinition ')' )? ')'

useOriginalSQLWhenTranslatingFailedClause ::=
  'USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED' '=' booleanLiteral

translatorType ::=
  identifier

propertiesDefinition ::=
  property (',' property)*

property ::=
  key '=' value

booleanLiteral ::=
  'TRUE' | 'FALSE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 说明

- `TYPE` 用于声明 SQL 翻译器实现及其 `PROPERTIES`。
- `USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED` 用于控制翻译失败时是否回退到原始 SQL。

### 示例

- 在翻译失败时回退到原始 SQL

```sql
ALTER SQL_TRANSLATOR RULE (
  TYPE(NAME='NATIVE', PROPERTIES('key'='value')),
  USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED=TRUE
);
```

- 不指定回退配置

```sql
ALTER SQL_TRANSLATOR RULE (
  TYPE(NAME='NATIVE', PROPERTIES('key'='value'))
);
```

### 保留字

`ALTER`、`SQL_TRANSLATOR`、`RULE`、`TYPE`、`NAME`、`PROPERTIES`、`USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
