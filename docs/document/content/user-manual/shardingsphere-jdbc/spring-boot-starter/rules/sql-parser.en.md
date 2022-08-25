+++
title = "SQL Parser"
weight = 6
+++

## Background
The configuration method of Spring Boot Starter is applicable to business scenarios using SpringBoot. In this way, the SpringBoot configuration initialization and bean management capabilities can be used to the greatest extent, so as to simplify code development.

## Parameters
```properties
spring.shardingsphere.rules.sql-parser.sql-comment-parse-enabled= # Whether to parse SQL comments

spring.shardingsphere.rules.sql-parser.sql-statement-cache.initial-capacity= # Initial capacity of SQL statement local cache
spring.shardingsphere.rules.sql-parser.sql-statement-cache.maximum-size= # Maximum capacity of SQL statement local cache

spring.shardingsphere.rules.sql-parser.parse-tree-cache.initial-capacity= # Initial capacity of parse tree local cache
spring.shardingsphere.rules.sql-parser.parse-tree-cache.maximum-size= # Maximum local cache capacity of parse tree
```

## Procedure
1. Set local cache configuration
2. Set parser configuration
3. use the parser engine to parse SQL

## Sample
```properties
spring.shardingsphere.rules.sql-parser.sql-comment-parse-enabled=true

spring.shardingsphere.rules.sql-parser.sql-statement-cache.initial-capacity=2000
spring.shardingsphere.rules.sql-parser.sql-statement-cache.maximum-size=65535

spring.shardingsphere.rules.sql-parser.parse-tree-cache.initial-capacity=128
spring.shardingsphere.rules.sql-parser.parse-tree-cache.maximum-size=1024
```

## Related References
- [JAVA API: SQL Parser](/en/user-manual/shardingsphere-jdbc/java-api/rules/sql-parser/)
- [YAML Configuration: SQL Parser](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
- [Spring Namespace: SQL Parser](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/sql-parser/)
