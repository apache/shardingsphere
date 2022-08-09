+++
pre = "<b>5.3. </b>"
title = "Kernel"
weight = 3
chapter = true
+++

## SQLRouter

### Fully-qualified class name

[`org.apache.shardingsphere.infra.route.SQLRouter`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-route/src/main/java/org/apache/shardingsphere/infra/route/SQLRouter.java)

### Definition

Used to process routing results

### Implementation classes

| *Configuration type*         | *Description*                                        | *Fully-qualified class name* |
| ---------------------------- | ---------------------------------------------------- | ---------------------------- |
| SingleTableRule.class        | Used to process single-table routing results         | [`org.apache.shardingsphere.singletable.route.SingleTableSQLRouter`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-single-table/shardingsphere-single-table-core/src/main/java/org/apache/shardingsphere/singletable/route/SingleTableSQLRouter.java) |
| ShardingRule.class           | Used to process sharding routing results             | [`org.apache.shardingsphere.sharding.route.engine.ShardingSQLRouter`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/route/engine/ShardingSQLRouter.java) |
| ReadwriteSplittingRule.class | Used to process read-write splitting routing results | [`org.apache.shardingsphere.readwritesplitting.route.ReadwriteSplittingSQLRouter`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/route/ReadwriteSplittingSQLRouter.java) |
| DatabaseDiscoveryRule.class  | Used to process database discovery routing results   | [`org.apache.shardingsphere.dbdiscovery.route.DatabaseDiscoverySQLRouter`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-core/src/main/java/org/apache/shardingsphere/dbdiscovery/route/DatabaseDiscoverySQLRouter.java) |
| ShadowRule.class             | Used to process shadow database routing results      | [`org.apache.shardingsphere.shadow.route.ShadowSQLRouter`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/route/ShadowSQLRouter.java) |

## SQLRewriteContextDecorator

### Fully-qualified class name

[`org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-rewrite/src/main/java/org/apache/shardingsphere/infra/rewrite/context/SQLRewriteContextDecorator.java)

### Definition

Used to handle SQL rewrite results

### Implementation classes

| *Configuration type* | *Description*                                  | *Fully-qualified class name* |
| -------------------- | ---------------------------------------------- | ---------------------------- |
| ShardingRule.class   | Used to process sharding SQL rewrite results   | [`org.apache.shardingsphere.sharding.rewrite.context.ShardingSQLRewriteContextDecorator`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/rewrite/context/ShardingSQLRewriteContextDecorator.java) |
| EncryptRule.class    | Used to process encryption SQL rewrite results | [`org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/rewrite/context/EncryptSQLRewriteContextDecorator.java) |

## SQLExecutionHook

### Fully-qualified class name

[`org.apache.shardingsphere.infra.executor.sql.hook.SQLExecutionHook`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-executor/src/main/java/org/apache/shardingsphere/infra/executor/sql/hook/SQLExecutionHook.java)

### Definition

SQL execution process listener

### Implementation classes

| *Configuration type*          | *Description*                     | *Fully-qualified class name* |
| ----------------------------- | --------------------------------- | ---------------------------- |
| Empty | Transaction hook of SQL execution | [`org.apache.shardingsphere.transaction.base.seata.at.TransactionalSQLExecutionHook`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-transaction/shardingsphere-transaction-type/shardingsphere-transaction-base/shardingsphere-transaction-base-seata-at/src/main/java/org/apache/shardingsphere/transaction/base/seata/at/TransactionalSQLExecutionHook.java) |

## ResultProcessEngine

### Fully-qualified class name

[`org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-merge/src/main/java/org/apache/shardingsphere/infra/merge/engine/ResultProcessEngine.java)

### Definition

Used to process result sets

### Implementation classes

| *Configuration type* | *Description*                                 | *Fully-qualified class name* |
| -------------------- | --------------------------------------------- | ---------------------------- |
| ShardingRule.class   | Used to handle sharding result set merge      | [`org.apache.shardingsphere.sharding.merge.ShardingResultMergerEngine`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/merge/ShardingResultMergerEngine.java) |
| EncryptRule.class    | Used to handle encrypted result set overrides | [`org.apache.shardingsphere.encrypt.merge.EncryptResultDecoratorEngine`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/merge/EncryptResultDecoratorEngine.java) |
