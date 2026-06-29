+++
title = "Properties Configuration"
weight = 1
chapter = true
+++

## Background

Apache ShardingSphere provides properties to configure system-level behavior.

## Parameters

| *Name*                                              | *Data Type* | *Description*                                                                                                                                                                                                                         | *Default Value*   |
|-----------------------------------------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| sql-show (?)                                        | boolean     | Whether to print SQL in logs. SQL logs include logical SQL, actual SQL, and SQL parse results. If enabled, logs use topic `org.apache.shardingsphere.sql` with log level INFO.                                                        | false             |
| sql-simple (?)                                      | boolean     | Whether to print SQL in a simplified style.                                                                                                                                                                                           | false             |
| kernel-executor-size (?)                            | int         | Worker thread pool size for SQL execution. Each ShardingSphereDataSource uses an independent thread pool, and different data sources in the same JVM do not share thread pools. `0` means unlimited.                                 | 0                 |
| max-connections-size-per-query (?)                  | int         | Maximum number of connections that one query request can use in each database instance.                                                                                                                                               | 1                 |
| max-union-size-per-datasource (?)                   | int         | Maximum UNION ALL size per data source for aggregate rewrite. When route units for one data source exceed this value, they are split into batches to restore parallel execution capability.                                          | Integer.MAX_VALUE |
| check-table-metadata-enabled (?)                    | boolean     | Whether to validate table metadata consistency when the application starts or metadata is updated.                                                                                                                                     | false             |
| load-table-metadata-batch-size (?)                  | int         | Number of table metadata entries loaded per batch when the application starts or refreshes table metadata.                                                                                                                             | 1000              |
| metadata-identifier-case-sensitivity (?)            | String      | Metadata identifier case sensitivity. Available values are `AUTO`, `SENSITIVE`, and `INSENSITIVE`.                                                                                                                                    | AUTO              |
| groovy-inline-expression-parsing-cache-max-size (?) | long        | Maximum size of the Groovy inline expression parsing cache.                                                                                                                                                                           | 1000              |

## Procedure

1. Properties are configured directly in the configuration file used by ShardingSphere-JDBC. The format is as follows:

```yaml
props:
    sql-show: true
```

## Notes

The default value of the `max-connections-size-per-query` configuration is 1, meaning each query request can only use one connection per database instance. If you adjust this parameter to enable memory-restricted mode (see [Memory-Strictly Mode](/en/reference/sharding/execute/#memory_strictly-mode) for details), ensure that your database's JDBC implementation supports streaming queries or can enable them. For example, in MySQL, you need to set `statement.setFetchSize(Integer.MIN_VALUE)` to achieve streaming queries.

## Sample

ShardingSphere examples contain property configurations for various scenarios. Please refer to: <https://github.com/apache/shardingsphere/tree/master/examples>
