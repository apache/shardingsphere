+++
title = "SQL Error Code"
weight = 1
chapter = true
+++

SQL error codes provide by standard `SQL State`, `Vendor Code` and `Reason`, which return to client when SQL execute error.

**the error codes are draft, still need to be adjusted.**

## Kernel Exception

### Meta data

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 08000     | 10001       | The URL \`%s\` is not recognized, please refer to the pattern \`%s\` |
| 42000     | 10002       | Can not support 3-tier structure for actual data node \`%s\` with JDBC \`%s\` |
| 42000     | 10003       | Unsupported SQL node conversion for SQL statement \`%s\` |
| 42000     | 10012       | Resource does not exist |
| 42000     | 10013       | Rule does not exist |
| HY004     | 10014       | Invalid format for actual data node \`%s\` |
| 42S02     | 10020       | Single table \`%s\` does not exist |
| 42S02     | 10021       | Schema \`%s\` does not exist |
| HY000     | 10022       | Can not load table with database name \`%s\` and data source name \`%s\` |
| 0A000     | 10023       | Can not drop schema \`%s\` because of contains tables |

### Data

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY004     | 11001       | Invalid value \`%s\` |
| HY004     | 11005       | Unsupported conversion stream charset \`%s\` |
| HY004     | 11006       | Unsupported conversion data type \`%s\` for value \`%s\` |

### Syntax

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 42000     | 12000       | You have an error in your SQL syntax: %s |
| 42000     | 12001       | SQL String can not be NULL or empty |
| 42000     | 12002       | Could not support variable \`%s\` |
| 0A000     | 12003       | DROP TABLE ... CASCADE is not supported |
| 42S02     | 12004       | Can not find column label \`%s\` |
| HV008     | 12005       | Column index \`%d\` is out of range |

### Connection

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 01000     | 13000       | Circuit break open, the request has been ignored |
| 08000     | 13001       | Can not get %d connections one time, partition succeed connection(%d) have released. Please consider increasing the \`maxPoolSize\` of the data sources or decreasing the \`max-connections-size-per-query\` in properties |
| 08000     | 13002       | Connection has been closed |
| 08000     | 13003       | Result set has been closed |
| HY000     | 13004       | Load datetime from database failed, reason: %s |
| HY004     | 13010       | Can not register driver, reason is: %s |

### Transaction

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 25000     | 14000       | Switch transaction type failed, please terminate the current transaction |
| 25000     | 14001       | Can not start new XA transaction in a active transaction |
| 25000     | 14002       | Failed to create \`%s\` XA data source |
| 25000     | 14003       | JDBC does not support operations across multiple logical databases in transaction |

### Lock

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY000     | 15000       | The table \`%s\` of schema \`%s\` is locked |
| HY000     | 15001       | The table \`%s\` of schema \`%s\` lock wait timeout of \`%s\` milliseconds exceeded |

### Audit

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 44000     | 16000       | SQL check failed, error message: %s |

### Cluster

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY000     | 17000       | Work ID assigned failed, which can not exceed 1024 |
| HY000     | 17001       | Can not find \`%s\` file for datetime initialize |
| HY000     | 17002       | File access failed, reason is: %s |

### Migration

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY000     | 18000       | Can not find pipeline job \`%s\` |
| HY000     | 18001       | Failed to get DDL for table \`%s\` |

## Feature Exception

### Data Sharding

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY000     | 20000       | Sharding algorithm class \`%s\` should be implement \`%s\` |
| 44000     | 20001       | Can not get uniformed table structure for logic table \`%s\`, it has different meta data of actual tables are as follows: %s |
| 42S02     | 20002       | Can not get route result, please check your sharding rule configuration |
| 44000     | 20003       | Can not find table rule with logic tables \`%s\` |
| 44000     | 20010       | Sharding value can't be null in insert statement |
| HY004     | 20011       | Found different types for sharding value \`%s\` |
| 44000     | 20012       | Can not update sharding value for table \`%s\` |
| 0A000     | 20013       | Can not support operation \`%s\` with sharding table \`%s\` |
| 0A000     | 20014       | Can not support DML operation with multiple tables \`%s\` |
| 0A000     | 20015       | The CREATE VIEW statement contains unsupported query statement |
| 42000     | 20016       | %s ... LIMIT can not support route to multiple data nodes |
| 44000     | 20017       | PREPARE statement can not support sharding tables route to same data sources |
| 42000     | 20018       | INSERT INTO ... SELECT can not support applying key generator with absent generate key column |
| 44000     | 20019       | The table inserted and the table selected must be the same or bind tables |
| 44000     | 20020       | Inline sharding algorithms expression \`%s\` and sharding column \`%s\` do not match |
| 44000     | 20021       | Actual data nodes must be configured for sharding table \`%s\` |
| 44000     | 20022       | Actual table \`%s.%s\` is not in table rule configuration |
| 44000     | 20023       | Can not find actual data source intersection for logic tables \`%s\` |
| 44000     | 20024       | Can not find binding actual table, data source is \`%s\`, logic table is \`%s\`, other actual table is \`%s\` |
| 44000     | 20025       | Can not find strategy for generate keys with table \`%s\` |
| HY004     | 20026       | Invalid %s, datetime pattern should be \`%s\`, value is \`%s\` |
| 42S01     | 20030       | Index \`%s\` already exists |
| 42S02     | 20031       | Index \`%s\` does not exist |
| 44000     | 20032       | Actual tables \`%s\` are in use |
| 42S01     | 20033       | View name has to bind to %s tables |
| 0A000     | 20034       | Alter view rename .. to .. statement should have same config for \`%s\` and \`%s\` |
| HY000     | 20050       | Routed target \`%s\` does not exist, available targets are \`%s\` |
| HY000     | 20051       | \`%s %s\` can not route correctly for %s \`%s\` |
| 42S02     | 20052       | Can not find data source in sharding rule, invalid actual data node \`%s\` |
| 34000     | 20053       | Can not get cursor name from fetch statement |

### Database HA

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY000     | 23000       | MGR plugin is not active in database \`%s\` |
| 44000     | 23001       | MGR is not in single primary mode in database \`%s\` |
| 44000     | 23002       | \`%s\` is not in MGR replication group member in database \`%s\` |
| 44000     | 23003       | Group name in MGR is not same with configured one \`%s\` in database \`%s\` |

### SQL Dialect Translator

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 42000     | 24020       | Can not support database \`%s\` in SQL translation |
| 42000     | 24021       | Translation error, SQL is: %s |

### Traffic Management

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 42S02     | 25000       | Can not get traffic execution unit |

### Data Encrypt

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| 44000     | 27000       | Can not find logic encrypt column by \`%s\` |
| 44000     | 27001       | Fail to find encrypt column \`%s\` from table \`%s\` |
| 44000     | 27002       | Altered column \`%s\` must use same encrypt algorithm with previous column \`%s\` in table \`%s\` |
| 42000     | 27020       | Insert value of index \`%s\` can not support for encrypt |
| 0A000     | 27021       | The SQL clause \`%s\` is unsupported in encrypt rule |
| HY004     | 27080       | Encrypt algorithm \`%s\` initialization failed, reason is: %s |

### Shadow Database

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY004     | 28010       | Shadow column \`%s\` of table \`%s\` does not support \`%s\` type |
| 42000     | 28020       | Insert value of index \`%s\` can not support for shadow |

## Other Exception

| SQL State | Vendor Code | Reason |
| --------- | ----------- | ------ |
| HY004     | 30000       | Unknown exception: %s |
| 0A000     | 30001       | Unsupported operation: %s |
| 0A000     | 30002       | Unsupported command: %s |
