+++
title = "SQL解析"
weight = 6
+++

## 配置项说明

```properties
spring.shardingsphere.rules.sql-parser.sql-comment-parse-enabled= # 是否解析SQL注释

spring.shardingsphere.rules.sql-parser.sql-statement-cache.initial-capacity= # SQL语句本地缓存初始容量
spring.shardingsphere.rules.sql-parser.sql-statement-cache.maximum-size= # SQL语句本地缓存最大容量
spring.shardingsphere.rules.sql-parser.sql-statement-cache.concurrency-level= # SQL语句本地缓存并发级别，最多允许线程并发更新的个数

spring.shardingsphere.rules.sql-parser.parse-tree-cache.initial-capacity= # 解析树本地缓存初始容量
spring.shardingsphere.rules.sql-parser.parse-tree-cache.maximum-size= # 解析树本地缓存最大容量
spring.shardingsphere.rules.sql-parser.parse-tree-cache.concurrency-level= # 解析树本地缓存并发级别，最多允许线程并发更新的个数
```
