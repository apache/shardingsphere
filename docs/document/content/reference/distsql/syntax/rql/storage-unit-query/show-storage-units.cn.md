+++
title = "SHOW STORAGE UNITS"
weight = 2
+++

### 描述

`SHOW STORAGE UNITS` 语法用于查询指定逻辑库已经注册的存储单元。

### 语法

```sql
ShowStorageUnit ::=
  'SHOW' 'STORAGE' 'UNITS' ('WHERE' 'USAGE_COUNT' '=' usageCount)? ('FROM' databaseName)?

usageCount ::=
  int

databaseName ::=
  identifier
```

### 特别说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`； 如未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列        | 说明        |
| --------- | ---------- |
| name      | 存储单元名称 |
| type      | 存储单元类型 |
| host      | 存储单元地址 |
| port      | 存储单元端口 |
| db        | 数据库名称   |
| attribute | 存储单元参数 |

### 示例

- 查询指定逻辑库中未被使用的存储单元

```sql
SHOW STORAGE UNITS WHERE USAGE_COUNT = 0 FROM test1;
```

```sql
mysql> SHOW STORAGE UNITS WHERE USAGE_COUNT = 0 FROM test1;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| su_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| su_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.03 sec)
```

- 查询当前逻辑库中未被使用的存储单元

```sql
SHOW STORAGE UNITS WHERE USAGE_COUNT = 0;
```

```sql
mysql> SHOW STORAGE UNITS WHERE USAGE_COUNT=0;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| su_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| su_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.01 sec)
```

- 查询指定逻辑库的存储单元

```sql
SHOW STORAGE UNITS FROM test1;
```

```sql
mysql> SHOW STORAGE UNITS FROM test1;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| su_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| su_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.01 sec)
```

- 查询当前逻辑库的存储单元 

```sql
SHOW STORAGE UNITS;
```

```sql
mysql> SHOW STORAGE UNITS;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| su_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| su_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"dataSourceProperties":{"maintainTimeStats":"false","rewriteBatchedStatements":"true","tinyInt1isBit":"false","cacheResultSetMetadata":"false","useServerPrepStmts":"true","netTimeoutForStreamingResults":"0","useSSL":"false","prepStmtCacheSqlLimit":"2048","elideSetAutoCommits":"true","cachePrepStmts":"true","serverTimezone":"UTC","zeroDateTimeBehavior":"round","prepStmtCacheSize":"8192","useLocalSessionState":"true","cacheServerConfiguration":"true"},"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.00 sec)
```
### 保留字

`SHOW`、`STORAGE`、`UNITS`、`WHERE`、`USAGE_COUNT`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)