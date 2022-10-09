+++
title = "SQL解析"
weight = 6
+++

## 背景信息
Spring 命名空间的配置方式，适用于传统的 Spring 项目，它通过命名空间 xml 配置文件的方式配置SQL 解析规则和属性。

## 参数解释

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.2.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.2.0.xsd)

\<sql-parser:rule />

| *名称*                    | *类型* | *说明*             |
|--------------------------|-------|--------------------|
| id                       | 属性   | Spring Bean Id     |
| sql-comment-parse-enable | 属性   | 是否解析 SQL 注释    |
| parse-tree-cache-ref     | 属性   | 解析树本地缓存名称    |
| sql-statement-cache-ref  | 属性   | SQL 语句本地缓存名称 |

\<sql-parser:cache-option />

| *名称*                       | *类型* | *说明*                              |
|-----------------------------| ----- |-------------------------------------|
| id                          | 属性  | 本地缓存配置项名称                      |
| initial-capacity            | 属性  | 本地缓存初始容量                        |
| maximum-size                | 属性  | 本地缓存最大容量                        |

## 操作步骤
1. 设置本地缓存配置
2. 设置解析配置
3. 使用解析引擎解析 SQL

## 配置示例
```xml
<sql-parser:rule id="sqlParseRule" sql-comment-parse-enable="true" parse-tree-cache-ref="parseTreeCache" sql-statement-cache-ref="sqlStatementCache" />
<sql-parser:cache-option id="sqlStatementCache" initial-capacity="1024" maximum-size="1024"/>
<sql-parser:cache-option id="parseTreeCache" initial-capacity="1024" maximum-size="1024"/>
```

## 相关参考
- [JAVA API：SQL 解析](/cn/user-manual/shardingsphere-jdbc/java-api/rules/sql-parser/)
- [YAML 配置：SQL 解析](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
- [Spring Boot Starter：SQL 解析](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/sql-parser/)
