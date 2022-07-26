+++
title = "SQL 解析"
weight = 7
+++

## 背景信息
SQL 解析 YAML 配置方式具有可读性高，使用简单的特点。通过 YAML 文件的方式，用户可以将代码与配置分离，并且根据需要方便地修改配置文件。

## 参数解释

```yaml
rules:
- !SQL_PARSER
  sqlCommentParseEnabled: # 是否解析 SQL 注释
  sqlStatementCache: # SQL 语句本地缓存配置项
    initialCapacity: # 本地缓存初始容量
    maximumSize: # 本地缓存最大容量
  parseTreeCache: # 解析树本地缓存配置项
    initialCapacity: # 本地缓存初始容量
    maximumSize: # 本地缓存最大容量
```

## 操作步骤

1. 设置本地缓存配置
2. 设置解析配置
3. 使用解析引擎解析 SQL

## 配置示例
```yaml
rules:
  - !SQL_PARSER
    sqlCommentParseEnabled: true
    sqlStatementCache:
      initialCapacity: 2000
      maximumSize: 65535
    parseTreeCache:
      initialCapacity: 128
      maximumSize: 1024
```

## 相关参考
- [JAVA API：SQL 解析](/cn/user-manual/shardingsphere-jdbc/java-api/rules/sql-parser/)
- [Spring Boot Starter：SQL 解析](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/sql-parser/)
- [Spring 命名空间：SQl解析](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/sql-parser/)
