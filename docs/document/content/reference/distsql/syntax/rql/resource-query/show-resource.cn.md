+++
title = "SHOW RESOURCE"
weight = 2
+++


### 描述

`SHOW RESOURCE` 语法用于查询指定逻辑库已经添加的资源。

### 语法

```SQL
ShowResource ::=
  'SHOW' 'DATABASE' 'RESOURCES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 特别说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`； 如未使用 `DATABASE` 则会提示 `No database selected`。

 ### 返回值说明

| 列        | 说明      |
| --------- | -------- |
| name      | 数据源名称 |
| type      | 数据源类型 |
| host      | 数据源地址 |
| port      | 数据源端口 |
| db        | 数据库名称 |
| attribute | 数据源参数 |

 ### 示例

- 查询指定逻辑库的资源
```sql
SHOW DATABASE RESOURCES FROM sharding_db;
```
```sql
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_0 | MySQL | 127.0.0.1 | 3306 | db_0 | 30000                           | 60000                     | 1800000                   | 50            | 1             | false     | {"dataSourceProperties":{"cacheServerConfiguration":"true","elideSetAutoCommits":"true","useServerPrepStmts":"true","cachePrepStmts":"true","rewriteBatchedStatements":"true","cacheResultSetMetadata":"false","useLocalSessionState":"true","maintainTimeStats":"false","prepStmtCacheSize":"200000","tinyInt1isBit":"false","prepStmtCacheSqlLimit":"2048","zeroDateTimeBehavior":"round","netTimeoutForStreamingResults":"0"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_1 | MySQL | 127.0.0.1 | 3306 | db_1 | 30000                           | 60000                     | 1800000                   | 50            | 1             | false     | {"dataSourceProperties":{"cacheServerConfiguration":"true","elideSetAutoCommits":"true","useServerPrepStmts":"true","cachePrepStmts":"true","rewriteBatchedStatements":"true","cacheResultSetMetadata":"false","useLocalSessionState":"true","maintainTimeStats":"false","prepStmtCacheSize":"200000","tinyInt1isBit":"false","prepStmtCacheSqlLimit":"2048","zeroDateTimeBehavior":"round","netTimeoutForStreamingResults":"0"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.26 sec)
```

- 查询当前逻辑库的资源 
```sql
SHOW DATABASE RESOURCES;
```
```sql
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_0 | MySQL | 127.0.0.1 | 3306 | db_0 | 30000                           | 60000                     | 1800000                   | 50            | 1             | false     | {"dataSourceProperties":{"cacheServerConfiguration":"true","elideSetAutoCommits":"true","useServerPrepStmts":"true","cachePrepStmts":"true","rewriteBatchedStatements":"true","cacheResultSetMetadata":"false","useLocalSessionState":"true","maintainTimeStats":"false","prepStmtCacheSize":"200000","tinyInt1isBit":"false","prepStmtCacheSqlLimit":"2048","zeroDateTimeBehavior":"round","netTimeoutForStreamingResults":"0"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_1 | MySQL | 127.0.0.1 | 3306 | db_1 | 30000                           | 60000                     | 1800000                   | 50            | 1             | false     | {"dataSourceProperties":{"cacheServerConfiguration":"true","elideSetAutoCommits":"true","useServerPrepStmts":"true","cachePrepStmts":"true","rewriteBatchedStatements":"true","cacheResultSetMetadata":"false","useLocalSessionState":"true","maintainTimeStats":"false","prepStmtCacheSize":"200000","tinyInt1isBit":"false","prepStmtCacheSqlLimit":"2048","zeroDateTimeBehavior":"round","netTimeoutForStreamingResults":"0"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.26 sec)
```
