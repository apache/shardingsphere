+++
title = "Alter SQL_PARSER Rule"
weight = 5
+++

### Description

The `ALTER SQL_PARSER RULE` syntax is used to alter the SQL parser rule configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterSqlParserRule ::=
  'ALTER' 'SQL_PARSER' 'RULE' '(' sqlParserRuleDefinition ')'

sqlParserRuleDefinition ::=
  commentDefinition? (',' parseTreeCacheDefinition)? (',' sqlStatementCacheDefinition)?

commentDefinition ::=
  'SQL_COMMENT_PARSE_ENABLED' '=' sqlCommentParseEnabled

parseTreeCacheDefinition ::=
  'PARSE_TREE_CACHE' '(' cacheOption ')'

sqlStatementCacheDefinition ::=
  'SQL_STATEMENT_CACHE' '(' cacheOption ')'

sqlCommentParseEnabled ::=
  boolean

cacheOption ::=
  ('INITIAL_CAPACITY' '=' initialCapacity)? (','? 'MAXIMUM_SIZE' '=' maximumSize)?

initialCapacity ::=
  int

maximumSize ::=
  int
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Note

- `SQL_COMMENT_PARSE_ENABLE`: specifies whether to parse the SQL comment.

- `PARSE_TREE_CACHE`: local cache configuration of the syntax tree.

- `SQL_STATEMENT_CACHE`: the local cache of SQL statement.

### Example

- Alter SQL parser rule

```sql
ALTER SQL_PARSER RULE (
  SQL_COMMENT_PARSE_ENABLED=false, 
  PARSE_TREE_CACHE(INITIAL_CAPACITY=128, MAXIMUM_SIZE=1024), 
  SQL_STATEMENT_CACHE(INITIAL_CAPACITY=2000, MAXIMUM_SIZE=65535)
);
```

### Reserved word

`ALTER`, `SQL_PARSER`, `RULE`, `SQL_COMMENT_PARSE_ENABLED`, `PARSE_TREE_CACHE`, `INITIAL_CAPACITY`, `MAXIMUM_SIZE`, `SQL_STATEMENT_CACHE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
