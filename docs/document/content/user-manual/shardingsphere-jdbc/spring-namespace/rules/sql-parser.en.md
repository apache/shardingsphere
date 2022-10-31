+++
title = "SQL Parser"
weight = 6
+++

## Background
Spring namespace's SQL parser configuration applies to traditional Spring projects. SQL parsing rules and attributes can be configured through the XML configuration files of the namespace.

## Parameters

Namespace：[http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.2.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.2.0.xsd)

\<sql-parser:rule />

| *Name*                   | *Type*    | *Description*           |
|--------------------------|-----------|----------------|
| id                       | Attribute | Spring Bean Id |
| sql-comment-parse-enable | Attribute | Whether to parse SQL comments    |
| parse-tree-cache-ref     | Attribute | Parse tree local cache name      |
| sql-statement-cache-ref  | Attribute | SQL statement local cache name   |

\<sql-parser:cache-option />

| *Name*                        | *Type* | *Description*               |
|-----------------------------| ----- |--------------------|
| id                          | Attribute  | Local cache configuration item name          |
| initial-capacity            | Attribute  | Initial capacity of local cache           |
| maximum-size                | Attribute  | Maximum capacity of local cache             |

## Procedure
1. Set local cache configuration.
2. Set parser configuration.
3. Parse SQL with a parsing engine.

## Sample
```xml
<sql-parser:rule id="sqlParseRule" sql-comment-parse-enable="true" parse-tree-cache-ref="parseTreeCache" sql-statement-cache-ref="sqlStatementCache" />
<sql-parser:cache-option id="sqlStatementCache" initial-capacity="1024" maximum-size="1024"/>
<sql-parser:cache-option id="parseTreeCache" initial-capacity="1024" maximum-size="1024"/>
```

## Related References
- [JAVA API: SQL Parser](/en/user-manual/shardingsphere-jdbc/java-api/rules/sql-parser/)
- [YAML Configuration: SQL Parser](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
- [Spring Boot Starter: SQL Parser](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/sql-parser/)
