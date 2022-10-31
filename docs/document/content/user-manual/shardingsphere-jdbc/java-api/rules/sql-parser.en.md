+++
title = "SQL Parser"
weight = 7
+++

## Background

SQL is the standard language for users to communicate with databases. The SQL parsing engine is responsible for parsing the SQL string into an abstract syntax tree for Apache ShardingSphere to understand and implement its incremental function.
Currently, MySQL, PostgreSQL, SQLServer, Oracle, openGauss and SQL dialects conforming to SQL92 specifications are supported. Due to the complexity of SQL syntax, there are still a few unsupported SQLs.
By using SQL parsing in the form of Java API, you can easily integrate into various systems and flexibly customize user requirements.

## Parameters

Class: org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration

Attributes:

| *name*                     | *DataType*      | *Description*                               |
| -------------------------- | --------------- | ------------------------------------------- |
| sqlCommentParseEnabled (?) | boolean         | Whether to parse SQL comments               |
| parseTreeCache (?)         | CacheOption     | Parse syntax tree local cache configuration |
| sqlStatementCache (?)      | CacheOption     | sql statement local cache configuration     |

## Cache option Configuration

Class：org.apache.shardingsphere.sql.parser.api.CacheOption

Attributes:

| *name*           | *DataType* | *Description*                   | *Default Value*                                                                                                         |
| ---------------- | ---------- | ------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| initialCapacity  | int        | Initial capacity of local cache | parser syntax tree local cache default value 128, SQL statement cache default value 2000                                |
| maximumSize(?)   | long       | Maximum capacity of local cache | The default value of local cache for parsing syntax tree is 1024, and the default value of sql statement cache is 65535 |

## Procedure

1. Set local cache configuration.
2. Set resolution configuration.
3. Use the parsing engine to parse SQL.
   
## Sample

```java
CacheOption cacheOption = new CacheOption(128, 1024L);
SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption);
ParseASTNode parseASTNode = parserEngine.parse("SELECT t.id, t.name, t.age FROM table1 AS t ORDER BY t.id DESC;", false);
SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "STATEMENT", false, new Properties());
MySQLStatement sqlStatement = visitorEngine.visit(parseASTNode);
System.out.println(sqlStatement.toString());
```
   
## Related References
- [YAML Configuration: SQL Parser](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
- [Spring Boot Starter: SQL Parser](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/sql-parser/)
- [Spring Namespace: SQL Parser](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/sql-parser/)
