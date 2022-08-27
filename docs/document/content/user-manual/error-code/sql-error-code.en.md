+++
title = "SQL Error Code"
weight = 1
chapter = true
+++

SQL error codes provide by standard `SQL State`, `Vendor Code` and `Reason`, which return to client when SQL execute error.

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 01000     | 10000       | Circuit break open, the request has been ignored |
| 08000     | 10001       | The URL \`%s\` is not recognized, please refer to the pattern \`%s\` |
| 42000     | 10002       | Can not support 3-tier structure for actual data node \`%s\` with JDBC \`%s\` |
| 42000     | 10003       | Unsupported SQL node conversion for SQL statement \`%s\` |
| HY004     | 10004       | Unsupported conversion data type \`%s\` for value \`%s\` |
| HY004     | 10100       | Can not register driver, reason is: %s |
| 34000     | 10200       | Can not get cursor name from fetch statement |
| 42000     | 11000       | You have an error in your SQL syntax: %s |
| 42000     | 11001       | configuration error |
| 42000     | 11002       | Resource does not exist |
| 42000     | 11003       | Rule does not exist |
| HY000     | 11004       | File access failed, reason is: %s |
| 42000     | 11200       | Can not support database \`%s\` in SQL translation |
| 42000     | 11201       | Translation error, SQL is: %s |
| 25000     | 11320       | Switch transaction type failed, please terminate the current transaction |
| 25000     | 11321       | JDBC does not support operations across multiple logical databases in transaction |
| 25000     | 11322       | Failed to create \`%s\` XA data source |
| 42S02     | 11400       | Can not get traffic execution unit |
| 42000     | 12000       | Unsupported command: %s |
| 44000     | 13000       | SQL check failed, error message: %s |
| HY000     | 14000       | The table \`%s\` of schema \`%s\` is locked |
| HY000     | 14001       | The table \`%s\` of schema \`%s\` lock wait timeout of %s ms exceeded |
| HY000     | 14010       | Can not find \`%s\` file for datetime initialize |
| HY000     | 14011       | Load datetime from database failed, reason: %s |
| HY000     | 15000       | Work ID assigned failed, which can not exceed 1024 |
| HY000     | 16000       | Can not find pipeline job \`%s\` |
| HY000     | 16001       | Failed to get DDL for table \`%s\` |
| HY000     | 20000       | Sharding algorithm class \`%s\` should be implement \`%s\` |
| 44000     | 20001       | Sharding value can't be null in insert statement |
| 44000     | 20002       | Can not get uniformed table structure for logic table \`%s\`, it has different meta data of actual tables are as follows: %s |
| 44000     | 20003       | Can not find table rule with logic tables \`%s\` |
| 0A000     | 20004       | Can not support operation \`%s\` with sharding table \`%s\` |
| 42S01     | 20005       | Index \`%s\` already exists |
| 42S02     | 20006       | Index \`%s\` does not exist |
| HY000     | 20007       | \`%s %s\` can not route correctly for %s \`%s\` |
| 42S02     | 20008       | Can not get route result, please check your sharding rule configuration |
| 42S01     | 20009       | View name has to bind to %s tables |
| 0A000     | 20010       | Can not support DML operation with multiple tables \`%s\` |
| 0A000     | 20011       | The CREATE VIEW statement contains unsupported query statement |
| 44000     | 20012       | Inline sharding algorithms expression \`%s\` and sharding column \`%s\` are not match |
| HY004     | 20013       | Found different types for sharding value \`%s\` |
| 44000     | 20014       | Actual tables \`%s\` are in use |
| 42S02     | 20015       | Can not find data source in sharding rule, invalid actual data node \`%s\` |
| HY000     | 20016       | Routed target \`%s\` does not exist, available targets are \`%s\` |
| 44000     | 20017       | Can not update sharding value for table \`%s\` |
| 42000     | 20018       | %s ... LIMIT can not support route to multiple data nodes |
| HY004     | 24000       | Encrypt algorithm \`%s\` initialize failed, reason is: %s |
| HY004     | 24001       | The SQL clause \`%s\` is unsupported in encrypt rule |
| 44000     | 24002       | Altered column \`%s\` must use same encrypt algorithm with previous column \`%s\` in table \`%s\` |
| 42000     | 24003       | Insert value of index \`%s\` can not support for encrypt |
| 44000     | 24004       | Can not find logic encrypt column by \`%s\` |
| HY004     | 25000       | Shadow column \`%s\` of table \`%s\` does not support \`%s\` type |
| 42000     | 25003       | Insert value of index \`%s\` can not support for shadow |
| 42000     | 30000       | Unknown exception: %s |
