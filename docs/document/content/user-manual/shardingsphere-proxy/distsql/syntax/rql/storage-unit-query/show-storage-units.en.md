+++
title = "SHOW STORAGE UNITS"
weight = 1
+++

### Description

The `SHOW STORAGE UNITS` syntax is used to query the storage units that have been added to the specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowStorageUnit ::=
  'SHOW' 'STORAGE' 'UNITS' ('WHERE' 'USAGE_COUNT' '=' usageCount)? ('FROM' databaseName)?

usageCount ::=
  int

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`; if `DATABASE` is not used, it will prompt `No database selected`.

### Return Value Description

| Column    | Description            |
|-----------|------------------------|
| name      | Storage unit name      |
| type      | Storage unit type      |
| host      | Storage unit host      |
| port      | Storage unit port      |
| db        | Database name          |
| attribute | Storage unit attribute |

 ### Example

- Query unused storage units for the specified database

```sql
SHOW STORAGE UNITS WHERE USAGE_COUNT = 0 FROM sharding_db;
```

```
mysql> SHOW STORAGE UNITS WHERE USAGE_COUNT = 0 FROM sharding_db;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.03 sec)
```

- Query unused storage units for current database

```sql
SHOW STORAGE UNITS WHERE USAGE_COUNT = 0;
```

```sql
mysql> SHOW STORAGE UNITS WHERE USAGE_COUNT=0;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.01 sec)
```
- Query storage units for the specified database

```sql
SHOW STORAGE UNITS FROM sharding_db;
```

```sql
mysql> SHOW STORAGE UNITS FROM sharding_db;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.01 sec)
```

- Query storage units for the current database

```sql
SHOW STORAGE UNITS;
```

```sql
mysql> SHOW STORAGE UNITS;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.00 sec)
```
### Reserved word

`SHOW`, `STORAGE`, `UNIT`, `WHERE`, `USAGE_COUNT`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)