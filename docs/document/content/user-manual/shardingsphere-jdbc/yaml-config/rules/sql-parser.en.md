+++
title = "SQL-parser"
weight = 8
+++

## Background

The SQL parser YAML configuration is readable and easy to use. The YAML files allow you to separate the code from the configuration, and easily modify the configuration file as needed.

## Parameters

```yaml
sqlParser:
  sqlStatementCache: # SQL statement local cache
    initialCapacity: # Initial capacity of local cache
    maximumSize: # Maximum capacity of local cache
  parseTreeCache: # Parse tree local cache
    initialCapacity: # Initial capacity of local cache
    maximumSize: # Maximum capacity of local cache
```

## Procedure
1. Set local cache configuration.
2. Set parser configuration.
3. Use a parsing engine to parse SQL.
   
## Sample
```yaml
sqlParser:
  sqlStatementCache:
    initialCapacity: 2000
    maximumSize: 65535
  parseTreeCache:
    initialCapacity: 128
    maximumSize: 1024
```

## Related References
- [JAVA API: SQL Parsing](/en/user-manual/shardingsphere-jdbc/java-api/rules/sql-parser/)
