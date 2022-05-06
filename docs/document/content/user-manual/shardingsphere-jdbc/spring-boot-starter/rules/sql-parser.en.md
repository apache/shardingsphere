+++
title = "SQL Parser"
weight = 6
+++

## Configuration Item Explanation

```properties
spring.shardingsphere.rules.sql-parser.sql-comment-parse-enabled= # Whether to parse SQL comments

spring.shardingsphere.rules.sql-parser.sql-statement-cache.initial-capacity= # Initial capacity of SQL statement local cache
spring.shardingsphere.rules.sql-parser.sql-statement-cache.maximum-size= # Maximum capacity of SQL statement local cache
spring.shardingsphere.rules.sql-parser.sql-statement-cache.concurrency-level= # SQL statement local cache concurrency level, the maximum number of concurrent updates allowed by threads

spring.shardingsphere.rules.sql-parser.parse-tree-cache.initial-capacity= # Initial capacity of parse tree local cache
spring.shardingsphere.rules.sql-parser.parse-tree-cache.maximum-size= # Maximum local cache capacity of parse tree
spring.shardingsphere.rules.sql-parser.parse-tree-cache.concurrency-level= # The local cache concurrency level of the parse tree. The maximum number of concurrent updates allowed by threads
```

