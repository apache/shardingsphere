+++
title = "Properties Configuration"
weight = 3
chapter = true
+++

## Introduction

Apache ShardingSphere provides the way of property configuration to configure system level configuration.

## Configuration Item Explanation

| *Name*                             | *Data Type* | *Description*                                                                                                                                                                                                                                                | *Default Value* |
| ---------------------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------- |
| sql-show (?)                       | boolean     | Whether show SQL or not in log. <br /> Print SQL details can help developers debug easier. The log details include: logic SQL, actual SQL and SQL parse result. <br /> Enable this property will log into log topic `ShardingSphere-SQL`, log level is INFO. | false           |
| sql-simple (?)                     | boolean     | Whether show SQL details in simple style.                                                                                                                                                                                                                    | false           |
| executor-size (?)                  | int         | The max thread size of worker group to execute SQL. One ShardingSphereDataSource will use a independent thread pool, it does not share thread pool even different data source in same JVM.                                                                   | infinite        |
| max-connections-size-per-query (?) | int         | Max opened connection size for each query.                                                                                                                                                                                                                   | 1               |
| check-table-metadata-enabled (?)   | boolean     | Whether validate table meta data consistency when application startup or updated.                                                                                                                                                                            | false           |
| proxy-frontend-flush-threshold (?) | int         | Flush threshold for every records from databases for ShardingSphere-Proxy.                                                                                                                                                                                   | 128             |
| proxy-opentracing-enabled (?)      | boolean     | Whether enable opentracing for ShardingSphere-Proxy.                                                                                                                                                                                                         | false           |
| proxy-hint-enabled (?)             | boolean     | Whether enable hint for ShardingSphere-Proxy. Using Hint will switch proxy thread mode from IO multiplexing to per connection per thread, which will reduce system throughput.                                                                               | false           |
| check-duplicate-table-enabled (?)  | boolean     | Whether validate duplicate table when application startup or updated.                                                                                                                                                                                        | false           |
| sql-comment-parse-enabled (?)      | boolean     | Whether parse the comment of SQL.                                                                                                                                                                                                                            | false           |
| proxy-frontend-executor-size (?)   | int         | Proxy frontend Netty executor size. The default value is 0, which means let Netty decide.                                                                                                                                                                    | 0               |
| proxy-backend-executor-suitable (?)| String      | Available options of proxy backend executor suitable: OLAP(default), OLTP. The OLTP option may reduce time cost of writing packets to client, but it may increase the latency of SQL execution if client connections are more than `proxy-frontend-netty-executor-size`, especially executing slow SQL.| OLAP    |
