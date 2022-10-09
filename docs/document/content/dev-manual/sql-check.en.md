+++
pre = "<b>5.11. </b>"
title = "SQL Checker"
weight = 11
chapter = true
+++

## SQLChecker

### Fully-qualified class name

[`org.apache.shardingsphere.infra.executor.check.SQLChecker`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-executor/src/main/java/org/apache/shardingsphere/infra/executor/check/SQLChecker.java)

### Definition

SQL checker class definition

### Implementation classes

| *Configuration Type* | *Description*          | *Fully-qualified class name* |
| -------------------- | ---------------------- | ---------------------------- |
| AuthorityRule.class  | Authority checker      | [`org.apache.shardingsphere.authority.checker.AuthorityChecker`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/checker/AuthorityChecker.java) |
| ShardingRule.class   | Sharding audit checker | [`org.apache.shardingsphere.sharding.checker.audit.ShardingAuditChecker`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/checker/audit/ShardingAuditChecker.java) |
