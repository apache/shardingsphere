+++
title = "SQL解析"
weight = 6
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.1.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.1.0.xsd)

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
| concurrency-level           | 属性  | 本地缓存并发级别，最多允许线程并发更新的个数 |
