+++
title = "SQL 解析"
weight = 6
+++

## 背景信息
Spring Boot Starter 的配置方式适用于使用 SpringBoot 的业务场景。使用这种方式，能够最大程度地利用 SpringBoot 配置初始化以及 Bean 管理的能力，从而达到简化代码开发的目的。

## 参数解释
```properties
spring.shardingsphere.rules.sql-parser.sql-comment-parse-enabled= # 是否解析 SQL 注释

spring.shardingsphere.rules.sql-parser.sql-statement-cache.initial-capacity= # SQL 语句本地缓存初始容量
spring.shardingsphere.rules.sql-parser.sql-statement-cache.maximum-size= # SQL 语句本地缓存最大容量

spring.shardingsphere.rules.sql-parser.parse-tree-cache.initial-capacity= # 解析树本地缓存初始容量
spring.shardingsphere.rules.sql-parser.parse-tree-cache.maximum-size= # 解析树本地缓存最大容量
```

## 操作步骤
1. 设置本地缓存配置
2. 设置解析配置
3. 使用解析引擎解析 SQL

## 配置示例
```properties
spring.shardingsphere.rules.sql-parser.sql-comment-parse-enabled=true

spring.shardingsphere.rules.sql-parser.sql-statement-cache.initial-capacity=2000
spring.shardingsphere.rules.sql-parser.sql-statement-cache.maximum-size=65535

spring.shardingsphere.rules.sql-parser.parse-tree-cache.initial-capacity=128
spring.shardingsphere.rules.sql-parser.parse-tree-cache.maximum-size=1024
```

## 相关参考
- [JAVA API：SQL 解析](/cn/user-manual/shardingsphere-jdbc/java-api/rules/sql-parser/)
- [YAML 配置：SQL 解析](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
- [Spring 命名空间：SQl解析](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/sql-parser/)
