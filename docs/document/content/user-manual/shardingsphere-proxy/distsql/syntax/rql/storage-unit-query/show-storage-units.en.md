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
  'SHOW' 'STORAGE' 'UNITS' ('FROM' databaseName)? showLike?

databaseName ::=
  identifier

showLike ::=
  'LIKE' likePattern

likePattern ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`; if `DATABASE` is not used, it will prompt `No database selected`.

### Return Value Description

| Column                          | Description                       |
|---------------------------------|-----------------------------------|
| name                            | Storage unit name                 |
| type                            | Storage unit type                 |
| host                            | Storage unit host                 |
| port                            | Storage unit port                 |
| db                              | Database name                     |
| connection_timeout_milliseconds | connection timeout (milliseconds) |
| idle_timeout_milliseconds       | idle timeout (milliseconds)       |
| max_lifetime_milliseconds       | max lifetime (milliseconds)       |
| max_pool_size                   | max pool size                     |
| min_pool_size                   | min pool size                     |
| read_only                       | read-only flag                    |
| other_attributes                | other attributes                  |

 ### Example

- Query storage units from current database

```sql
mysql> SHOW STORAGE UNITS;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                     |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"keepaliveTime":0,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"keepaliveTime":0,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.01 sec)
```

- Query storage units from specified database

```sql
mysql> SHOW STORAGE UNITS FROM sharding_db;
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                     |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_1 | MySQL | 127.0.0.1 | 3306 | db1  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"keepaliveTime":0,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
| ds_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"keepaliveTime":0,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.01 sec)
```

- Query storage units with like clause

```sql
mysql> SHOW STORAGE UNITS LIKE '%_0';
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes                                                                                                                                                                                                                     |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_0 | MySQL | 127.0.0.1 | 3306 | db0  | 30000                           | 60000                     | 2100000                   | 50            | 1             | false     | {"healthCheckProperties":{},"initializationFailTimeout":1,"validationTimeout":5000,"keepaliveTime":0,"leakDetectionThreshold":0,"registerMbeans":false,"allowPoolSuspension":false,"autoCommit":true,"isolateInternalQueries":false} |
+------+-------+-----------+------+------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 rows in set (0.01 sec)
```

### Reserved word

`SHOW`, `STORAGE`, `UNITS`, `FROM`, `LIKE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)