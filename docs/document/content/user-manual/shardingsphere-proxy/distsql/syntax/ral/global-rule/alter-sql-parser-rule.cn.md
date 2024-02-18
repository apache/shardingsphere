+++
title = "ALTER SQL_PARSER RULE"
weight = 5
+++

### 描述

`ALTER SQL_PARSER RULE` 语法用于修改解析引擎规则配置。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterSqlParserRule ::=
  'ALTER' 'SQL_PARSER' 'RULE' '(' sqlParserRuleDefinition ')'

sqlParserRuleDefinition ::=
  parseTreeCacheDefinition? (',' sqlStatementCacheDefinition)?

parseTreeCacheDefinition ::=
  'PARSE_TREE_CACHE' '(' cacheOption ')'

sqlStatementCacheDefinition ::=
  'SQL_STATEMENT_CACHE' '(' cacheOption ')'

cacheOption ::=
  ('INITIAL_CAPACITY' '=' initialCapacity)? (','? 'MAXIMUM_SIZE' '=' maximumSize)?

initialCapacity ::=
  int

maximumSize ::=
  int
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `PARSE_TREE_CACHE`：语法树本地缓存配置

- `SQL_STATEMENT_CACHE`：SQL 语句本地缓存配置项

### 示例

- 修改 SQL 解析引擎规则

```sql
ALTER SQL_PARSER RULE (
  PARSE_TREE_CACHE(INITIAL_CAPACITY=128, MAXIMUM_SIZE=1024), 
  SQL_STATEMENT_CACHE(INITIAL_CAPACITY=2000, MAXIMUM_SIZE=65535)
);
```

### 保留字

`ALTER`、`SQL_PARSER`、`RULE`、`PARSE_TREE_CACHE`、`INITIAL_CAPACITY`、`MAXIMUM_SIZE`、`SQL_STATEMENT_CACHE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)