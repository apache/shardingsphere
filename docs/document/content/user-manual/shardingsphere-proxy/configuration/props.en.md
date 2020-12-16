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
| acceptor-size (?)                  | int         | The max thread size of accepter group to accept TCP connections.                                                                                                                                                                                             | CPU * 2         |
| executor-size (?)                  | int         | The max thread size of worker group to execute SQL. One ShardingSphereDataSource will use a independent thread pool, it does not share thread pool even different data source in same JVM.                                                                   | infinite        |
| max-connections-size-per-query (?) | int         | Max opened connection size for each query.                                                                                                                                                                                                                   | 1               |
| check-table-metadata-enabled (?)   | boolean     | Whether validate table meta data consistency when application startup or updated.                                                                                                                                                                            | false           |
| query-with-cipher-column (?)       | boolean     | Whether query with cipher column for data encrypt. User you can use plaintext to query if have.                                                                                                                                                              | true            |
| proxy-frontend-flush-threshold (?) | int         | Flush threshold for every records from databases for ShardingSphere-Proxy.                                                                                                                                                                                   | 128             |
| proxy-transaction-type (?)         | String      | Default transaction type of ShardingSphere-Proxy. Include: LOCAL, XA and BASE.                                                                                                                                                                               | LOCAL           |
| proxy-opentracing-enabled (?)      | boolean     | Whether enable opentracing for ShardingSphere-Proxy.                                                                                                                                                                                                         | false           |
| proxy-hint-enabled (?)             | boolean     | Whether enable hint for ShardingSphere-Proxy. Using Hint will switch proxy thread mode from IO multiplexing to per connection per thread, which will reduce system throughput.                                                                               | false           |
| transaction-manager-type (?)       | String      | Transaction manager type. Include: Atomikos, Narayana.                                                                                                                                                                                                       | Atomikos        |
