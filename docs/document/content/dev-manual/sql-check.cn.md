+++
pre = "<b>5.11. </b>"
title = "SQL 检查"
weight = 11
chapter = true
+++

### 全限定类名

[`org.apache.shardingsphere.infra.executor.check.SQLChecker`](https://github.com/apache/shardingsphere/blob/master/infra/executor/src/main/java/org/apache/shardingsphere/infra/executor/check/SQLChecker.java)

### 定义

SQL 检查定义接口

### 已知实现

| *配置标识*           | *详细说明*    | *全限定类名* |
|---------------------|--------------| ----------- |
| AuthorityRule.class | 权限检查器    | [`org.apache.shardingsphere.authority.checker.AuthorityChecker`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/checker/AuthorityChecker.java) |
| ShardingRule.class  | 分片审计检查器 | [`org.apache.shardingsphere.sharding.checker.audit.ShardingAuditChecker`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/checker/audit/ShardingAuditChecker.java) |
