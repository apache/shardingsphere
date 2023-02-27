+++
title = " ShardingSphere 5.2.0: Audit for sharding intercepts unreasonable requests in multi-shards scenarios"
weight = 76
chapter = true 

+++

![img](https://shardingsphere.apache.org/blog/img/2022_10_14_ShardingSphere_5.2.0_Audit_for_sharding_intercepts_unreasonable_requests_in_multi-shards_scenarios1.png)

## 1. Background

Thanks to our continuous review of the [ShardingSphere](https://shardingsphere.apache.org/)’s community feedback that we use to develop features such as data sharding and read/write splitting, we found that some users create a large number of shards when using the data sharding feature.

In such cases, there can be 1,000 physical tables corresponding to a sharding logical table, which largely disturbs users.

For instance, a `SELECT * FROM t_order` statement will lead to a full-route, which is obviously not the case for [OLTP](https://shardingsphere.apache.org/blog/en/material/2022_04_26_how_to_use_shardingsphere-proxy_in_real_production_scenarios_your_quick_start_guide/). This SQL can be placed in another Proxy to avoid blocking other requests.

However, if users are not familiar with Proxy, or write a `where` condition and don't know that sharding is not supported in this condition, a full-route is still required.

A full route can lower the performance of Proxy and even result in the failure of a reasonable request. Imagine that there are 1000 shards in a physical database, if they are executed in parallel, 1,000 connections are needed — and if in serial, the request can lead to a timeout. In this regard, community users requested whether the unreasonable request can be intercepted directly.

We’ve considered the issue for a while. If we simply block the full-route operation, we just need to check it in the code and add a switch to the configuration file. On the other hand, if the user later needs to set a table to read-only or requires the update operation to carry a `limit`, does that mean we need to change the code and configuration again? This obviously goes against the pluggable logic of Proxy.

In response to the above problems, the [recently released Apache ShardingSphere 5.2.0](https://faun.pub/apache-shardingsphere-5-2-0-is-released-bringing-new-cloud-native-possibilities-8d674d964a93?source=your_stories_page-------------------------------------) provides users with SQL audit for the sharding function. The audit can either be an interception operation or a statistical operation. Similar to the sharding and unique key generation algorithms, the audit algorithm is also plugin-oriented, user-defined, and configurable.

Next, we will elaborate on the implementation logic for data sharding’s audit, with specific SQL examples.

## **2. Audit for sharding interface**

The entrance to Apache ShardingSphere’s audit is in the `org.apache.shardingsphere.infra.executor.check.SQLCheckEngine` class, which will invoke the `check` method of the `SQLChecker` interface. Currently, ShardingSphere audit contains audit for permission (verify username and password) and audit for sharding.

Here we focus on the parent interface implemented in `ShardingAuditChecker` of audit for sharding.

![img](https://shardingsphere.apache.org/blog/img/2022_10_14_ShardingSphere_5.2.0_Audit_for_sharding_intercepts_unreasonable_requests_in_multi-shards_scenarios2.png)

We can learn its working principles quickly through viewing the `check` code of `org.apache.shardingsphere.sharding.checker.audit.ShardingAuditChecker`.

```java
public interface ShardingAuditAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Sharding audit algorithm SQL check.
     *
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @param grantee grantee
     * @param database database
     * @return SQL check result
     */
    SQLCheckResult check(SQLStatementContext<?> sqlStatementContext, List<Object> parameters, Grantee grantee, ShardingSphereDatabase database);
}
```

This method obtains the audit strategies of all the sharding tables involved and invokes the audit algorithms configured in each sharding table audit strategy. If an audit algorithm fails to pass, an exception is displayed to the user.

Some users may wonder what `disableAuditNames` does here. The audit for sharding also allows users to skip this process. In some cases, users may need to execute SQL that should have been blocked by the audit, and they are aware of the impact of this SQL.

For this reason, we provide `Hint: disableAuditNames` to skip audit interception, which will be described with practical examples later on. The Proxy Administrators can configure `allowHintDisable` to control whether to allow users to skip this process. The default value is `true`, indicating that Hint-based skip is allowed.

## **3. Audit for sharding algorithm**

The audit for sharding algorithm interface `org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm` is inherited from SPI class `ShardingSphereAlgorithm`. It inherits `type` and `props` properties and defines its own `check` method. If you‘re looking to customize your own audit algorithm, just implement the interface and add it to `INF.services`.

![img](https://shardingsphere.apache.org/blog/img/2022_10_14_ShardingSphere_5.2.0_Audit_for_sharding_intercepts_unreasonable_requests_in_multi-shards_scenarios3.png)

```java
public interface ShardingAuditAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Sharding audit algorithm SQL check.
     *
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @param grantee grantee
     * @param database database
     * @return SQL check result
     */
    SQLCheckResult check(SQLStatementContext<?> sqlStatementContext, List<Object> parameters, Grantee grantee, ShardingSphereDatabase database);
}
```

Apache ShardingSphere implements a general audit for sharding algorithm `org.apache.shardingsphere.sharding.algorithm.audit.DMLShardingConditionsShardingAuditAlgorithm`, namely the above-mentioned SQL statement that intercepts the full-route.

The algorithm makes decisions by determining whether the sharding condition is `null`. Of course, it won't intercept broadcast tables and non-sharding tables.

```java
public final class DMLShardingConditionsShardingAuditAlgorithm implements ShardingAuditAlgorithm {
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public SQLCheckResult check(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final Grantee grantee, final ShardingSphereDatabase database) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            ShardingRule rule = database.getRuleMetaData().getSingleRule(ShardingRule.class);
            if (rule.isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames())
                    || sqlStatementContext.getTablesContext().getTableNames().stream().noneMatch(rule::isShardingTable)) {
                return new SQLCheckResult(true, "");
            }
            ShardingConditionEngine shardingConditionEngine = ShardingConditionEngineFactory.createShardingConditionEngine(sqlStatementContext, database, rule);
            if (shardingConditionEngine.createShardingConditions(sqlStatementContext, parameters).isEmpty()) {
                return new SQLCheckResult(false, "Not allow DML operation without sharding conditions");
            }
        }
        return new SQLCheckResult(true, "");
    }
    
    @Override
    public String getType() {
        return "DML_SHARDING_CONDITIONS";
    }
}
```

Here we’d like to introduce another audit for sharding algorithm: `LimitRequiredShardingAuditAlgorithm`. This algorithm can intercept SQL without carrying `limit` in the `update` and `delete` operations.

As this algorithm is less universal, it is not currently integrated into Apache ShardingSphere. As you can see, it is very easy to implement a custom algorithm, which is why we need to design the audit for sharding framework. Thanks to its plugin-oriented architecture, ShardingSphere boasts great scalability.

```java
public final class LimitRequiredShardingAuditAlgorithm implements ShardingAuditAlgorithm {
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public SQLCheckResult check(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final Grantee grantee, final ShardingSphereDatabase database) {
        if (sqlStatementContext instanceof UpdateStatementContext && !((MySQLUpdateStatement) sqlStatementContext.getSqlStatement()).getLimit().isPresent()) {
            return new SQLCheckResult(false, "Not allow update without limit");
        }
        if (sqlStatementContext instanceof DeleteStatementContext && !((MySQLDeleteStatement) sqlStatementContext.getSqlStatement()).getLimit().isPresent()) {
            return new SQLCheckResult(false, "Not allow delete without limit");
        }
        return new SQLCheckResult(true, "");
    }
    
    @Override
    public String getType() {
        return "LIMIT_REQUIRED";
    }
}
```

## **4. Using audit for sharding**

Audit for sharding requires you to configure audit strategy for logical tables. To help you quickly get started, its configuration is the same with that of the sharding algorithm and the sharding key value generator.

There is an algorithm definition and strategy definition, and default audit strategy is also supported. If the audit strategy is configured in the logical table, it takes effect only for the logical table.

If `defaultAuditStrategy` is configured in the logical table, it takes effect fo all the logical tables under the sharding rule. `Auditors` are similar to `ShardingAlgorithms`, `auditStrategy` to `databaseStrategy`, and `defaultAuditStrategy` to `defaultDatabaseStrategy` or `defaultTableStrategy`.

Please refer to the following configuration. Only the configuration of audit for sharding is displayed. You need to configure the sharding algorithm and data source by yourself.

```sql
rules:
  - !SHARDING
    tables:
      t_order:
        actualDataNodes: ds_${0..1}.t_order_${0..1}
        auditStrategy:
          auditorNames:
            - sharding_key_required_auditor
          allowHintDisable: true    defaultAuditStrategy:
      auditorNames:
        - sharding_key_required_auditor
      allowHintDisable: true    auditors:
      sharding_key_required_auditor:
        type: DML_SHARDING_CONDITIONS
```

**Step 1:** Execute a query operation. An error is displayed as the audit strategy for intercepting the full-database route is configured.

```mysql
mysql> select * from t_order;
ERROR 13000 (44000): SQL check failed, error message: Not allow DML operation without sharding conditions
```

**Step 2:** Add `HINT.` The name of the `HINT` is `/* ShardingSphere hint: disableAuditNames */`，and `disableAuditNames` is followed by the `auditorsNames` configured in the preceding command.

If there are multiple names, separate them with spaces such as `/* ShardingSphere hint: disableAuditNames=auditName1 auditName2*/`. After using `HINT`, we can see that the SQL operation is successfully executed.

```mysql
mysql> /* ShardingSphere hint: disableAuditNames=sharding_key_required_auditor */ select * from t_order;
+----------+---------+------------+--------+
| order_id | user_id | address_id | status |
+----------+---------+------------+--------+
|       30 |      20 |         10 | 20     |
|       32 |      22 |         10 | 20     |
+----------+---------+------------+--------+
2 rows in set (0.01 sec)
```

**Note:** `HINT` requires you to modify the `server.yaml` configuration of Proxy. In addition, if you are using MySQL terminal to connect to Proxy directly, you need to add the `-c` property — otherwise, `HINT `comments will be filtered out of the MySQL terminal and will not be parsed by Proxy on the backend.

```sql
rules:
  - !SQL_PARSER
    sqlCommentParseEnabled: true
    sqlStatementCache:
      initialCapacity: 2000
      maximumSize: 65535
    parseTreeCache:
      initialCapacity: 128
      maximumSize: 1024
props:
  proxy-hint-enabled: truemysql -uroot -proot -h127.0.0.1 -P3307  -c
```

## **5. DistSQL with audit for sharding**

Currently, as you can see from the [release notes](https://github.com/apache/shardingsphere/releases/tag/5.2.0) Apache ShardingSphere 5.2.0 supports the following [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/) with audit for sharding function.

```sql
CREATE SHARDING AUDITOR
ALTER SHARDING AUDITOR
SHOW SHARDING AUDIT ALGORITHMS
```

The following DistSQL will be supported in future releases:

```sql
DROP SHARDING AUDITOR
SHOW UNUSED SHARDING AUDIT ALGORITHMS
CREATE SHARDING TABLE RULE # including AUDIT_STRATEGY
```

This post introduced how audit for sharding works with specific examples. I believe you already have basic understanding of this function, and you can use it whenever you need or use custom algorithm.

You are also welcome to submit general algorithms to the community. If you have any ideas you’d like to contribute or you encounter any issues with your ShardingSphere, feel free to post them on [GitHub](https://github.com/apache/shardingsphere).

# Author

Huang Ting, a technology engineer at [Tencent](https://www.tencent.com/en-us/) Financial Technology (FiT) & [ShardingSphere Committer](https://shardingsphere.apache.org/community/en/team/).

He is mainly responsible for the R&D of Proxy-related audit for sharding and transaction features.
