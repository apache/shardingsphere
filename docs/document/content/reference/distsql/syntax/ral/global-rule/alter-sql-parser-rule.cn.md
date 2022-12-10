+++
title = "ALTER SQL_PARSER RULE"
weight = 6
+++

### 描述

`ALTER SQL_PARSER RULE` 语法用于修改解析引擎规则配置

### 语法

```sql
AlterSqlParserRule ::=
  'ALTER' 'SQL_PARSER' 'RULE' 'SQL_COMMENT_PARSE_ENABLE' '=' sqlCommentParseEnable ',' 'PARSE_TREE_CACHE' parseTreeCacheDefination ',' 'SQL_STATEMENT_CACHE' sqlStatementCacheDefination

sqlCommentParseEnable ::=
  boolean

parseTreeCacheDefination ::=
  '(' 'INITIAL_CAPACITY' '=' initialCapacity ',' 'MAXIMUM_SIZE' '=' maximumSize ',' 'CONCURRENCY_LEVEL' '=' concurrencyLevel ')'

sqlStatementCacheDefination ::=
  '(' 'INITIAL_CAPACITY' '=' initialCapacity ',' 'MAXIMUM_SIZE' '=' maximumSize ',' 'CONCURRENCY_LEVEL' '=' concurrencyLevel ')'

initialCapacity ::=
  int

maximumSize ::=
  int

concurrencyLevel ::=
  int
```

### 补充说明

- `SQL_COMMENT_PARSE_ENABLE`：是否解析 SQL 注释

- `PARSE_TREE_CACHE`：语法树本地缓存配置

- `SQL_STATEMENT_CACHE`：SQL 语句本地缓存配置项

### 示例

- 修改解析引擎规则配置

```sql
ALTER SQL_PARSER RULE 
  SQL_COMMENT_PARSE_ENABLE=false, 
  PARSE_TREE_CACHE(INITIAL_CAPACITY=10, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=1), 
  SQL_STATEMENT_CACHE(INITIAL_CAPACITY=11, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=100)；
```

### 保留字

`ALTER`、`SQL_PARSER`、`RULE`、`SQL_COMMENT_PARSE_ENABLE`、`PARSE_TREE_CACHE`、`INITIAL_CAPACITY`、`MAXIMUM_SIZE`、`CONCURRENCY_LEVEL`、`SQL_STATEMENT_CACHE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)