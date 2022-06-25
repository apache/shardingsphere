+++
title = "SQL-parser"
weight = 6
+++

## Configuration Item Explanation

```yaml
rules:
- !SQL_PARSER
  sqlCommentParseEnabled: # Whether to parse SQL comments 
  sqlStatementCache: # SQL statement local cache
    initialCapacity: # Initial capacity of local cache
    maximumSize: # Maximum capacity of local cache
  parseTreeCache: # Parse tree local cache
    initialCapacity: # Initial capacity of local cache
    maximumSize: # Maximum capacity of local cache
```
