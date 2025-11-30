+++
title = "Properties Configuration"
weight = 1
chapter = true
+++

## Background

Apache ShardingSphere provides the way of property configuration to configure system level configuration.

## Parameters

| *Name*                             | *Data Type* | *Description*                                                                                                                                                                                                                                               | *Default Value* |
|------------------------------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| sql-show (?)                       | boolean     | Whether show SQL or not in log. <br /> Print SQL details can help developers debug easier. The log details include: logic SQL, actual SQL and SQL parse result. <br /> Enable this property will log into log topic `org.apache.shardingsphere.sql`, log level is INFO | false           |
| sql-simple (?)                     | boolean     | Whether show SQL details in simple style                                                                                                                                                                                                                    | false           |
| kernel-executor-size (?)           | int         | The max thread size of worker group to execute SQL. One ShardingSphereDataSource will use a independent thread pool, it does not share thread pool even different data source in same JVM                                                                   | infinite        |
| max-connections-size-per-query (?) | int         | Max opened connection size for each query                                                                                                                                                                                                                   | 1               |
| check-table-metadata-enabled (?)   | boolean     | Whether validate table meta data consistency when application startup or updated                                                                                                                                                                            | false           |
| load-table-metadata-batch-size (?) | int         | The number of table metadata loaded at a time when application startup or refreshes table metadata                                                                                                                                                          | 1000            |

## Procedure

1. Properties configuration is directly configured in the profile used by ShardingSphere-JDBC. The format is as follows:

```yaml
props:
    sql-show: true
```

## Notes

The default value of the `max-connections-size-per-query` configuration is 1, meaning each query request can only use one connection per database instance. If you adjust this parameter to enable memory-restricted mode (see [Memory-Strictly Mode](/en/reference/sharding/execute/#memory_strictly-mode) for details), ensure that your database's JDBC implementation supports streaming queries or can enable them. For example, in MySQL, you need to set `statement.setFetchSize(Integer.MIN_VALUE)` to achieve streaming queries.

## Sample

The example of ShardingSphere warehouse contains property configurations of various scenarios. Please refer to: <https://github.com/apache/shardingsphere/tree/master/examples>
