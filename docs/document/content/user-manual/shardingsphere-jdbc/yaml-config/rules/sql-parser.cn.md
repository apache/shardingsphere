+++
title = "SQL 解析"
weight = 6
+++

## 配置项说明

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
