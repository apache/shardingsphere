+++
title = "ALTER SQL_PARSER RULE"
weight = 6
+++

### Description

The `ALTER SQL_PARSER RULE` syntax is used to alter sql parser rule configuration.

### Syntax

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

### Supplement

- `SQL_COMMENT_PARSE_ENABLE`: whether to parse the SQL comment

- `PARSE_TREE_CACHE`: local cache configuration of syntax tree

- `SQL_STATEMENT_CACHE`: local cache of SQL statement

### Example

- Alter sql parser rule

```sql
ALTER SQL_PARSER RULE 
  SQL_COMMENT_PARSE_ENABLE=false, 
  PARSE_TREE_CACHE(INITIAL_CAPACITY=10, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=1), 
  SQL_STATEMENT_CACHE(INITIAL_CAPACITY=11, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=100)；
```

### Reserved word

`ALTER`, `SQL_PARSER`, `RULE`, `SQL_COMMENT_PARSE_ENABLE`, `PARSE_TREE_CACHE`, `INITIAL_CAPACITY`, `MAXIMUM_SIZE`, `CONCURRENCY_LEVEL`, `SQL_STATEMENT_CACHE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
