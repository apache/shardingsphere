+++
title = "SQL解析"
weight = 6
+++

## 配置入口

类名称：org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration

可配置属性：

| *名称*                        | *数据类型*        | *说明*               |
|-----------------------------|-------------------|---------------------|
| sqlCommentParseEnabled (?)  | boolean           | 是否解析 SQL 注释     |
| parseTreeCache (?)          | CacheOption       | 解析语法树本地缓存配置  |
| sqlStatementCache (?)       | CacheOption       | sql语句本地缓存配置    |

## 本地缓存配置

类名称：org.apache.shardingsphere.sql.parser.api.CacheOption

可配置属性：

| *名称*                    | *数据类型*   | *说明*                                       | *默认值*                                    |
|-------------------------|-------------|---------------------------------------------|--------------------------------------------|
| initialCapacity         | int         | 本地缓存初始容量                               | 语法树本地缓存默认值128，sql语句缓存默认值2000   |
| maximumSize             | long        | 本地缓存最大容量                               | 语法树本地缓存默认值1024，sql语句缓存默认值65535 |
| concurrencyLevel        | int         | 本地缓存并发级别，最多允许线程并发更新的个数        | 4                                          |
