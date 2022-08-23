+++
title = "SQL 错误码"
weight = 1
chapter = true
+++

SQL 错误码以标准的 SQL State，Vendor Code 和详细错误信息提供，在 SQL 执行错误时返回给客户端。

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 01000     | 10000       | Circuit break open, the request has been ignored |
| 08000     | 10001       | The URL \`%s\` is not recognized, please refer to the pattern \`%s\` |
| 42000     | 10002       | Can not support 3-tier structure for actual data node \`%s\` with JDBC \`%s\` |
| 42000     | 10003       | Unsupported SQL node conversion for SQL statement \`%s\` |
| HY004     | 10004       | Unsupported conversion data type \`%s\` for value \`%s\` |
| HY004     | 10100       | Can not register driver, reason is: %s |
| 42000     | 11000       | You have an error in your SQL syntax: %s |
| 42000     | 11001       | configuration error |
| 42000     | 11002       | Resource does not exist |
| 42000     | 11003       | Rule does not exist |
| HY000     | 11004       | File access failed, reason is: %s |
| 42000     | 11200       | Can not support database \`%s\` in SQL translation |
| 42000     | 11201       | Translation error, SQL is: %s |
| 42000     | 12000       | Unsupported command: %s |
| 44000     | 13000       | SQL check failed, error message: %s |
| HY000     | 14000       | The table \`%s\` of schema \`%s\` is locked |
| HY000     | 14001       | The table \`%s\` of schema \`%s\` lock wait timeout of %s ms exceeded |
| HY000     | 14010       | Can not find \`%s\` file for datetime initialize |
| HY000     | 14011       | Load datetime from database failed, reason: %s |
| HY000     | 15000       | Work ID assigned failed, which can not exceed 1024 |
| HY000     | 16000       | Can not find pipeline job \`%s\` |
| HY000     | 16001       | Failed to get DDL for table \`%s\` |
| HY004     | 24000       | Encrypt algorithm \`%s\` initialize failed, reason is: %s |
| HY004     | 24001       | The SQL clause \`%s\` is unsupported in encrypt rule |
| 44000     | 24002       | Altered column \`%s\` must use same encrypt algorithm with previous column \`%s\` in table \`%s\` |
| 42000     | 24003       | Insert value of index \`%s\` can not support for encrypt |
| 44000     | 24004       | Can not find logic encrypt column by \`%s\` |
| HY004     | 25000       | Shadow column \`%s\` of table \`%s\` does not support \`%s\` type |
| 42000     | 30000       | Unknown exception: %s |
