+++
pre = "<b>5.5. </b>"
title = "SQL 审计"
weight = 5
chapter = true
+++

## SQLAuditor

### 全限定类名

[`org.apache.shardingsphere.infra.executor.audit.SQLAuditor`](https://github.com/apache/shardingsphere/blob/master/infra/executor/src/main/java/org/apache/shardingsphere/infra/executor/audit/SQLAuditor.java)

### 定义

SQL 审计定义接口

### 已知实现

| *配置标识*   | *详细说明*     | *全限定类名*                                                                                                                                                                                                                         |
|----------|------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sharding | 分片 SQL 审计器 | [`org.apache.shardingsphere.sharding.auditor.ShardingSQLAuditor`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/auditor/ShardingSQLAuditor.java) |
