+++
title = "SQL 错误码"
weight = 1
chapter = true
+++

SQL 错误码以标准的 SQL State，Vendor Code 和详细错误信息提供，在 SQL 执行错误时返回给客户端。

**目前内容为草稿，错误码仍可能调整。**

## 内核异常

### 元数据

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 42000     | 10000       | Resource does not exist |
| 08000     | 10001       | The URL \`%s\` is not recognized, please refer to the pattern \`%s\` |
| 42000     | 10002       | Can not support 3-tier structure for actual data node \`%s\` with JDBC \`%s\` |
| HY004     | 10003       | Invalid format for actual data node \`%s\` |
| 42000     | 10004       | Unsupported SQL node conversion for SQL statement \`%s\` |
| 42000     | 10010       | Rule does not exist |
| 42S02     | 10020       | Schema \`%s\` does not exist |
| 42S02     | 10021       | Single table \`%s\` does not exist |
| HY000     | 10022       | Can not load table with database name \`%s\` and data source name \`%s\` |
| 0A000     | 11030       | Can not drop schema \`%s\` because of contains tables |

### 数据

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| HY004     | 11000       | Invalid value \`%s\` |
| HY004     | 11001       | Unsupported conversion data type \`%s\` for value \`%s\` |
| HY004     | 11010       | Unsupported conversion stream charset \`%s\` |

### 语法

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 42000     | 12000       | You have an error in your SQL syntax: %s |
| 42000     | 12001       | SQL String can not be NULL or empty |
| 42000     | 12002       | Could not support variable \`%s\` |
| 42S02     | 12003       | Can not find column label \`%s\` |
| HV008     | 12004       | Column index \`%d\` is out of range |
| 0A000     | 12010       | DROP TABLE ... CASCADE is not supported |

### 连接

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 08000     | 13000       | Can not register driver, reason is: %s |
| 01000     | 13010       | Circuit break open, the request has been ignored |
| 08000     | 13020       | Can not get %d connections one time, partition succeed connection(%d) have released. Please consider increasing the \`maxPoolSize\` of the data sources or decreasing the \`max-connections-size-per-query\` in properties |
| 08000     | 13030       | Connection has been closed |
| 08000     | 13031       | Result set has been closed |
| HY000     | 13090       | Load datetime from database failed, reason: %s |

### 事务

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 25000     | 14000       | Switch transaction type failed, please terminate the current transaction |
| 25000     | 14100       | JDBC does not support operations across multiple logical databases in transaction |
| 25000     | 14200       | Can not start new XA transaction in a active transaction |
| 25000     | 14201       | Failed to create \`%s\` XA data source |

### 锁

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| HY000     | 15000       | The table \`%s\` of schema \`%s\` is locked |
| HY000     | 15001       | The table \`%s\` of schema \`%s\` lock wait timeout of \`%s\` milliseconds exceeded |

### 审计

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 44000     | 16000       | SQL check failed, error message: %s |

### 集群

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| HY000     | 17000       | Work ID assigned failed, which can not exceed 1024 |
| HY000     | 17001       | Can not find \`%s\` file for datetime initialize |
| HY000     | 17002       | File access failed, reason is: %s |

### 迁移

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 44000     | 18001       | Created process configuration already existed |
| 44000     | 18002       | Altered process configuration does not exist |
| HY000     | 18020       | Failed to get DDL for table \`%s\` |
| HY000     | 18080       | Can not find pipeline job \`%s\` |
| HY000     | 18081       | Job has already started |
| HY000     | 18082       | Sharding count of job \`%s\` is 0 |
| HY000     | 18083       | Can not split range for table \`%s\`, reason: %s |
| HY000     | 18084       | Target table \`%s\` is not empty |
| 01007     | 18085       | Source data source lacks %s privilege(s) |
| HY000     | 18086       | Source data source required \`%s = %s\`, now is \`%s\` |
| HY000     | 18087       | User \`%s\` does exist |
| HY000     | 18090       | Importer job write data failed |
| HY000     | 18091       | Can not poll event because of binlog sync channel already closed |
| HY000     | 18092       | Task \`%s\` execute failed |

## 功能异常

### 数据分片

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 44000     | 20000       | Can not find table rule with logic tables \`%s\` |
| 44000     | 20001       | Can not get uniformed table structure for logic table \`%s\`, it has different meta data of actual tables are as follows: %s |
| 42S02     | 20002       | Can not find data source in sharding rule, invalid actual data node \`%s\` |
| 44000     | 20003       | Data nodes must be configured for sharding table \`%s\` |
| 44000     | 20004       | Actual table \`%s.%s\` is not in table rule configuration |
| 44000     | 20005       | Can not find binding actual table, data source is \`%s\`, logic table is \`%s\`, other actual table is \`%s\` |
| 44000     | 20006       | Actual tables \`%s\` are in use |
| 42S01     | 20007       | Index \`%s\` already exists |
| 42S02     | 20008       | Index \`%s\` does not exist |
| 42S01     | 20009       | View name has to bind to %s tables |
| 44000     | 20020       | Sharding value can't be null in insert statement |
| HY004     | 20021       | Found different types for sharding value \`%s\` |
| HY004     | 20022       | Invalid %s, datetime pattern should be \`%s\`, value is \`%s\` |
| 0A000     | 20040       | Can not support operation \`%s\` with sharding table \`%s\` |
| 44000     | 20041       | Can not update sharding value for table \`%s\` |
| 0A000     | 20042       | The CREATE VIEW statement contains unsupported query statement |
| 44000     | 20043       | PREPARE statement can not support sharding tables route to same data sources |
| 44000     | 20044       | The table inserted and the table selected must be the same or bind tables |
| 0A000     | 20045       | Can not support DML operation with multiple tables \`%s\` |
| 42000     | 20046       | %s ... LIMIT can not support route to multiple data nodes |
| 44000     | 20047       | Can not find actual data source intersection for logic tables \`%s\` |
| 42000     | 20048       | INSERT INTO ... SELECT can not support applying key generator with absent generate key column |
| 0A000     | 20049       | Alter view rename .. to .. statement should have same config for \`%s\` and \`%s\` |
| HY000     | 20060       | \`%s %s\` can not route correctly for %s \`%s\` |
| 42S02     | 20061       | Can not get route result, please check your sharding rule configuration |
| 34000     | 20062       | Can not get cursor name from fetch statement |
| HY000     | 20080       | Sharding algorithm class \`%s\` should be implement \`%s\` |
| HY000     | 20081       | Routed target \`%s\` does not exist, available targets are \`%s\` |
| 44000     | 20082       | Inline sharding algorithms expression \`%s\` and sharding column \`%s\` do not match |
| 44000     | 20090       | Can not find strategy for generate keys with table \`%s\` |

### 数据库高可用

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| HY000     | 20380       | MGR plugin is not active in database \`%s\` |
| 44000     | 20381       | MGR is not in single primary mode in database \`%s\` |
| 44000     | 20382       | \`%s\` is not in MGR replication group member in database \`%s\` |
| 44000     | 20383       | Group name in MGR is not same with configured one \`%s\` in database \`%s\` |

### SQL 方言转换

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 42000     | 20440       | Can not support database \`%s\` in SQL translation |
| 42000     | 20441       | Translation error, SQL is: %s |

### 流量治理

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 42S02     | 20500       | Can not get traffic execution unit |

### 数据加密

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 44000     | 20700       | Can not find logic encrypt column by \`%s\` |
| 44000     | 20701       | Fail to find encrypt column \`%s\` from table \`%s\` |
| 44000     | 20702       | Altered column \`%s\` must use same encrypt algorithm with previous column \`%s\` in table \`%s\` |
| 42000     | 20740       | Insert value of index \`%s\` can not support for encrypt |
| 0A000     | 20741       | The SQL clause \`%s\` is unsupported in encrypt rule |
| HY004     | 20780       | Encrypt algorithm \`%s\` initialization failed, reason is: %s |

### 影子库

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| HY004     | 20820       | Shadow column \`%s\` of table \`%s\` does not support \`%s\` type |
| 42000     | 20840       | Insert value of index \`%s\` can not support for shadow |

## 其他异常

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| HY004     | 30000       | Unknown exception: %s |
| 0A000     | 30001       | Unsupported operation: %s |
| 0A000     | 30002       | Unsupported command: %s |
