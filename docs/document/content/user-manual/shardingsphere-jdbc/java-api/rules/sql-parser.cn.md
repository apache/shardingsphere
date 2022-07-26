+++
title = "SQL 解析"
weight = 7
+++

## 背景信息
SQL 是使用者与数据库交流的标准语言。 SQL 解析引擎负责将 SQL 字符串解析为抽象语法树，供 Apache ShardingSphere 理解并实现其增量功能。
目前支持 MySQL, PostgreSQL, SQLServer, Oracle, openGauss 以及符合 SQL92 规范的 SQL 方言。 由于 SQL 语法的复杂性，目前仍然存在少量不支持的 SQL。
通过 Java API 形式使用 SQL 解析，可以方便得集成进入各种系统，灵活定制用户需求。

## 参数解释

类名称：org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration

可配置属性：

| *名称*                       | *数据类型*        | *说明*               |
|-----------------------------|-------------------|---------------------|
| sqlCommentParseEnabled (?)  | boolean           | 是否解析 SQL 注释     |
| parseTreeCache (?)          | CacheOption       | 解析语法树本地缓存配置  |
| sqlStatementCache (?)       | CacheOption       | SQL 语句本地缓存配置    |

## 本地缓存配置

类名称：org.apache.shardingsphere.sql.parser.api.CacheOption

可配置属性：

| *名称*                    | *数据类型*   | *说明*                                       | *默认值*                                    |
|-------------------------|-------------|---------------------------------------------|--------------------------------------------|
| initialCapacity         | int         | 本地缓存初始容量                               | 语法树本地缓存默认值 128，SQL 语句缓存默认值 2000   |
| maximumSize             | long        | 本地缓存最大容量                               | 语法树本地缓存默认值 1024，SQL 语句缓存默认值 65535 |

## 操作步骤

1. 设置本地缓存配置
2. 设置解析配置
3. 使用解析引擎解析 SQL

## 配置示例

```java
CacheOption cacheOption = new CacheOption(128, 1024L);
SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption);
ParseASTNode parseASTNode = parserEngine.parse("SELECT t.id, t.name, t.age FROM table1 AS t ORDER BY t.id DESC;", false);
SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "STATEMENT", false, new Properties());
MySQLStatement sqlStatement = visitorEngine.visit(parseASTNode);
System.out.println(sqlStatement.toString());
```

## 相关参考
- [YAML 配置：SQL 解析](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
- [Spring Boot Starter：SQL 解析](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/sql-parser/)
- [Spring 命名空间：SQl解析](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/sql-parser/)
