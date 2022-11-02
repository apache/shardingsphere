+++
pre = "<b>5.3. </b>"
title = "内核"
weight = 3
chapter = true
+++

## SQLRouter

### 全限定类名

[`org.apache.shardingsphere.infra.route.SQLRouter`](https://github.com/apache/shardingsphere/blob/master/infra/route/src/main/java/org/apache/shardingsphere/infra/route/SQLRouter.java)

### 定义

用于处理路由结果

### 已知实现

| *配置标识*              | *详细说明*                | *全限定类名*                                               |
| ---------------------- | ------------------------ | ---------------------------------------------------------- |
| SingleTableRule        | 用于处理单表路由结果       | [`org.apache.shardingsphere.singletable.route.SingleTableSQLRouter`](https://github.com/apache/shardingsphere/blob/master/kernel/single-table/core/src/main/java/org/apache/shardingsphere/singletable/route/SingleTableSQLRouter.java)      |
| ShardingRule           | 用于处理分片路由结果       | [`org.apache.shardingsphere.sharding.route.engine.ShardingSQLRouter`](https://github.com/apache/shardingsphere/blob/master/infra/route/src/main/java/org/apache/shardingsphere/infra/route/SQLRouter.java)      |
| ReadwriteSplittingRule | 用于处理读写分离路由结果   | [`org.apache.shardingsphere.readwritesplitting.route.ReadwriteSplittingSQLRouter`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/route/ReadwriteSplittingSQLRouter.java)                          |
| DatabaseDiscoveryRule  | 用于处理数据库发现路由结果 | [`org.apache.shardingsphere.dbdiscovery.route.DatabaseDiscoverySQLRouter`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/core/src/main/java/org/apache/shardingsphere/dbdiscovery/route/DatabaseDiscoverySQLRouter.java) |
| ShadowRule             | 用于处理影子库路由结果     | [`org.apache.shardingsphere.shadow.route.ShadowSQLRouter`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/route/ShadowSQLRouter.java)      |

## SQLRewriteContextDecorator

### 全限定类名

[`org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator`](https://github.com/apache/shardingsphere/blob/master/infra/rewrite/src/main/java/org/apache/shardingsphere/infra/rewrite/context/SQLRewriteContextDecorator.java)

### 定义

用于处理 SQL 改写结果

### 已知实现

| *配置标识*          | *详细说明*              | *全限定类名*                                               |
| ------------------ | ----------------------- | --------------------------------------------------------- |
| ShardingRule       | 用于处理分片 SQL 改写结果 | [`org.apache.shardingsphere.sharding.rewrite.context.ShardingSQLRewriteContextDecorator`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/rewrite/context/ShardingSQLRewriteContextDecorator.java)               |
| EncryptRule        | 用于处理加密 SQL 改写结果 | [`org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/rewrite/context/EncryptSQLRewriteContextDecorator.java) |

## SQLExecutionHook

### 全限定类名

[`org.apache.shardingsphere.infra.executor.sql.hook.SQLExecutionHook`](https://github.com/apache/shardingsphere/blob/master/infra/executor/src/main/java/org/apache/shardingsphere/infra/executor/sql/hook/SQLExecutionHook.java)

### 定义

SQL 执行过程监听器

### 已知实现

| *配置标识*          | *详细说明*                              | *全限定类名*                                                                |
| ------------------ | --------------------------------------- | -------------------------------------------------------------------------- |
| 无                 | 基于事务的 SQL 执行过程监听器             | [`org.apache.shardingsphere.transaction.base.seata.at.TransactionalSQLExecutionHook`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/base/seata-at/src/main/java/org/apache/shardingsphere/transaction/base/seata/at/TransactionalSQLExecutionHook.java)               |


## ResultProcessEngine

### 全限定类名

[`org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine`](https://github.com/apache/shardingsphere/blob/master/infra/merge/src/main/java/org/apache/shardingsphere/infra/merge/engine/ResultProcessEngine.java)

### 定义

用于处理结果集

### 已知实现

| *配置标识*          | *详细说明*           | *全限定类名*                                            |
| ------------------ | -------------------- | ------------------------------------------------------ |
| ShardingRule       | 用于处理分片结果集归并 | [`org.apache.shardingsphere.sharding.merge.ShardingResultMergerEngine`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/merge/ShardingResultMergerEngine.java)               |
| EncryptRule        | 用于处理加密结果集改写 | [`org.apache.shardingsphere.encrypt.merge.EncryptResultDecoratorEngine`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/merge/ShardingResultMergerEngine.java) |
