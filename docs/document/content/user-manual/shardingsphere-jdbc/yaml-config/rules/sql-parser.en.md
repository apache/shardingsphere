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
    concurrencyLevel: # Local cache concurrency level, the maximum number of concurrent updates allowed by threads
  parseTreeCache: # Parse tree local cache
    initialCapacity: # Initial capacity of local cache
    maximumSize: # Maximum capacity of local cache
    concurrencyLevel: # Local cache concurrency level, the maximum number of concurrent updates allowed by threads
```
