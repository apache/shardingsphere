+++
title = "SQL 错误码"
weight = 1
chapter = true
+++

SQL 错误码以标准的 SQL State，Vendor Code 和详细错误信息提供，在 SQL 执行错误时返回给客户端。

**目前内容为草稿，错误码仍可能调整。**

## 内核异常

### 元数据

| SQL State | Vendor Code | 错误信息                                                                           |
|-----------|-------------|--------------------------------------------------------------------------------|
| 42000     | 10000       | There is no storage unit in database \`%s\`.                                   |
| 08000     | 10001       | The URL \`%s\` is not recognized, please refer to the pattern \`%s\`.          |
| 42000     | 10002       | Can not support 3-tier structure for actual data node \`%s\` with JDBC \`%s\`. |
| HY004     | 10003       | Invalid format for actual data node \`%s\`.                                    |
| 42000     | 10004       | Unsupported SQL node conversion for SQL statement \`%s\`.                      |
| HY000     | 10005       | Column '%s' in field list is ambiguous.                                        |
| 42S02     | 10006       | Unknown column '%s' in 'field list'.                                           |
| 42000     | 10010       | Rule does not exist.                                                           |
| 42S02     | 10020       | Schema \`%s\` does not exist.                                                  |
| 42S02     | 10021       | Single table \`%s\` does not exist.                                            |
| HY000     | 10022       | Can not load table with database name \`%s\` and data source name \`%s\`.      |
| 0A000     | 10030       | Can not drop schema \`%s\` because of contains tables.                         |
| 0A000     | 10040       | Unsupported storage type of \`%s.%s\`.                                         |

### 数据

| SQL State | Vendor Code | 错误信息                                                      |
|-----------|-------------|-----------------------------------------------------------|
| HY004     | 11000       | Invalid value \`%s\`.                                     |
| HY004     | 11001       | Unsupported conversion data type \`%s\` for value \`%s\`. |
| HY004     | 11010       | Unsupported conversion stream charset \`%s\`.             |

### 语法

| SQL State | Vendor Code | 错误信息                                         |
|-----------|-------------|----------------------------------------------|
| 42000     | 12000       | You have an error in your SQL syntax: %s     |
| 42000     | 12001       | Can not accept SQL type \`%s\`.              |
| 42000     | 12002       | SQL String can not be NULL or empty.         |
| 42000     | 12010       | Can not support variable \`%s\`.             |
| 42S02     | 12011       | Can not find column label \`%s\`.            |
| 42S02     | 12012       | Can not find driver url provider for \`%s`\. |
| HV008     | 12020       | Column index \`%d\` is out of range.         |
| 0A000     | 12100       | DROP TABLE ... CASCADE is not supported.     |

### 连接

| SQL State | Vendor Code | 错误信息                                                                                                                                                                                                                        |
|-----------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 08000     | 13000       | Can not register driver, reason is: %s                                                                                                                                                                                      |
| 08000     | 13001       | Can not register SQL federation driver, reason is: %s                                                                                                                                                                       |
| 01000     | 13010       | Circuit break open, the request has been ignored.                                                                                                                                                                           |
| 01000     | 13011       | The cluster status is read-only.                                                                                                                                                                                            |
| 01000     | 13012       | The cluster status is unavailable.                                                                                                                                                                                          |
| 08000     | 13020       | Can not get %d connections one time, partition succeed connection(%d) have released. Please consider increasing the \`maxPoolSize\` of the data sources or decreasing the \`max-connections-size-per-query\` in properties. |
| 08000     | 13030       | Connection has been closed.                                                                                                                                                                                                 |
| 08000     | 13031       | Result set has been closed.                                                                                                                                                                                                 |
| HY000     | 13090       | Load datetime from database failed, reason: %s                                                                                                                                                                              |

### 事务

| SQL State | Vendor Code | 错误信息                                                                               |
|-----------|-------------|------------------------------------------------------------------------------------|
| 25000     | 14000       | Switch transaction type failed, please terminate the current transaction.          |
| 25000     | 14001       | Can not find transaction manager of \`%s\`.                                        |
| 25000     | 14002       | Transaction timeout should more than 0s.                                           |
| 25000     | 14100       | JDBC does not support operations across multiple logical databases in transaction. |
| 25000     | 14200       | Can not start new XA transaction in a active transaction.                          |
| 25000     | 14201       | Failed to create \`%s\` XA data source.                                            |
| 25000     | 14202       | Max length of xa unique resource name \`%s\` exceeded: should be less than 45.     |
| 25000     | 14203       | Check privileges failed on data source, reason is: \`%s\`                          |
| 25000     | 14204       | Failed to create XA transaction manager, requires \`%s\` privileges                |
| 25000     | 14205       | Close transaction manager failed, \`%s\`                                           |
| 25000     | 14301       | ShardingSphere Seata-AT transaction has been disabled.                             |
| 25000     | 14302       | Please config application id within seata.conf file.                               |

### 锁

| SQL State | Vendor Code | 错误信息                                                                                 |
|-----------|-------------|--------------------------------------------------------------------------------------|
| HY000     | 15000       | The table \`%s\` of schema \`%s\` is locked.                                         |
| HY000     | 15001       | The table \`%s\` of schema \`%s\` lock wait timeout of \`%s\` milliseconds exceeded. |

### 审计

| SQL State | Vendor Code | 错误信息                                 |
|-----------|-------------|--------------------------------------|
| 44000     | 16000       | SQL audit failed, error message: %s. |
| 44000     | 16001       | Hint data source: %s is not exist.   |

### 权限

| SQL State | Vendor Code | 错误信息                              |
|-----------|-------------|-----------------------------------|
| 44000     | 16500       | Access denied for operation `%s`. |

### 集群

| SQL State | Vendor Code | 错误信息                                                |
|-----------|-------------|-----------------------------------------------------|
| HY000     | 17000       | Work ID assigned failed, which can not exceed 1024. |
| HY000     | 17002       | File access failed, reason is: %s                   |
| HY000     | 17010       | Cluster persist repository error, reason is: %s     |

### 迁移

| SQL State | Vendor Code | 错误信息                                                                               |
|-----------|-------------|------------------------------------------------------------------------------------|
| 42S02     | 18002       | There is no rule in database \`%s\`.                                               |
| 44000     | 18003       | Mode configuration does not exist.                                                 |
| 44000     | 18004       | Target database name is null. You could define it in DistSQL or select a database. |
| 22023     | 18005       | There is invalid parameter value: `%s`.                                            |
| HY000     | 18020       | Failed to get DDL for table \`%s\`.                                                |
| 42S01     | 18030       | Duplicate storage unit names \`%s\`.                                               |
| 42S02     | 18031       | Storage units names \`%s\` do not exist.                                           |
| 08000     | 18051       | Data check table \`%s\` failed.                                                    |
| 0A000     | 18052       | Unsupported pipeline database type \`%s\`.                                         |
| 0A000     | 18053       | Unsupported CRC32 data consistency calculate algorithm with database type \`%s\`.  |
| 0A000     | 18054       | Unsupported mode type \`%s\`.                                                      |
| HY000     | 18080       | Can not find pipeline job \`%s\`.                                                  |
| HY000     | 18081       | Job has already started.                                                           |
| HY000     | 18082       | Sharding count of job \`%s\` is 0.                                                 |
| HY000     | 18083       | Can not split by range for table \`%s\`, reason is: %s                             |
| HY000     | 18084       | Can not split by unique key \`%s\` for table \`%s\`, reason is: %s                 |
| HY000     | 18085       | Target table \`%s\` is not empty.                                                  |
| 01007     | 18086       | Source data source lacks %s privilege(s).                                          |
| HY000     | 18087       | Source data source required \`%s = %s\`, now is \`%s\`.                            |
| HY000     | 18088       | User \`%s\` does exist.                                                            |
| 08000     | 18089       | Check privileges failed on source data source, reason is: %s                       |
| 08000     | 18090       | Data sources can not connect, reason is: %s                                        |
| HY000     | 18091       | Importer job write data failed.                                                    |
| 08000     | 18092       | Get binlog position failed by job \`%s\`, reason is: %s                            |
| HY000     | 18093       | Can not poll event because of binlog sync channel already closed.                  |
| HY000     | 18095       | Can not find consistency check job of \`%s\`.                                      |
| HY000     | 18096       | Uncompleted consistency check job \`%s\` exists.                                   |
| HY000     | 18200       | Not find stream data source table.                                                 |
| HY000     | 18201       | CDC server exception, reason is: %s.                                               |
| HY000     | 18202       | CDC login failed, reason is: %s                                                    |

### DistSQL

| SQL State | Vendor Code | 错误信息                                                        |
|-----------|-------------|-------------------------------------------------------------|
| 44000     | 19000       | Can not process invalid storage units, error message is: %s |
| 44000     | 19001       | Storage units \`%s\` do not exist in database \`%s\`.       |
| 44000     | 19002       | There is no storage unit in the database \`%s\`.            |
| 44000     | 19003       | Storage units \`%s\` is still used by \`%s\`.               |
| 44000     | 19004       | Duplicate storage unit names \`%s\`.                        |
| 44000     | 19100       | Invalid \`%s\` rule \`%s\`, error message is: %s            |
| 44000     | 19101       | %s rules \`%s\` do not exist in database \`%s\`.            |
| 44000     | 19102       | %s rules \`%s\` in database \`%s\` are still in used.       |
| 44000     | 19103       | %s rule \`%s\` has been enabled in database \`%s\`.         |
| 44000     | 19104       | %s rule \`%s\` has been disabled in database \`%s\`.        |
| 44000     | 19105       | Duplicate %s rule names \`%s\` in database \`%s\`.          |
| 44000     | 19150       | Invalid %s algorithm(s) \`%s\`.                             |
| 44000     | 19151       | %s algorithm(s) \`%s\` do not exist in database \`%s\`.     |
| 44000     | 19152       | %s algorithms \`%s\` in database \`%s\` are still in used.  |
| 44000     | 19153       | Duplicate %s algorithms \`%s\` in database \`%s\`.          |

## 功能异常

### 数据分片

| SQL State | Vendor Code | 错误信息                                                                                                                             |
|-----------|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| 44000     | 20000       | Can not find table rule with logic tables \`%s\`.                                                                                |
| 44000     | 20001       | Can not get uniformed table structure for logic table \`%s\`, it has different meta data of actual tables are as follows: %s     |
| 42S02     | 20002       | Can not find data source in sharding rule, invalid actual data node \`%s\`.                                                      |
| 44000     | 20003       | Data nodes must be configured for sharding table \`%s\`.                                                                         |
| 44000     | 20004       | Actual table \`%s.%s\` is not in table rule configuration.                                                                       |
| 44000     | 20005       | Can not find binding actual table, data source is \`%s\`, logic table is \`%s\`, other actual table is \`%s\`.                   |
| 44000     | 20006       | Actual tables \`%s\` are in use.                                                                                                 |
| 42S01     | 20007       | Index \`%s\` already exists.                                                                                                     |
| 42S02     | 20008       | Index \`%s\` does not exist.                                                                                                     |
| 42S01     | 20009       | View name has to bind to %s tables.                                                                                              |
| 44000     | 20010       | \`%s\` algorithm does not exist in database \`%s\`.                                                                              |
| 44000     | 20011       | \`%s\` configuration does not exist in database \`%s\`.                                                                          |
| 44000     | 20012       | Invalid binding table configuration in ShardingRuleConfiguration.                                                                |
| 44000     | 20013       | Can not find sharding rule.                                                                                                      |
| 44000     | 20014       | Only allowed 0 or 1 sharding strategy configuration.                                                                             |
| 44000     | 20020       | Sharding value can't be null in sql statement.                                                                                   |
| HY004     | 20021       | Found different types for sharding value \`%s\`.                                                                                 |
| HY004     | 20022       | Invalid %s, datetime pattern should be \`%s\`, value is \`%s\`.                                                                  |
| 44000     | 20023       | Sharding value %s subtract stop offset %d can not be less than start offset %d.                                                  |
| 44000     | 20024       | %s value \`%s\` must implements Comparable.                                                                                      |
| 0A000     | 20040       | Can not support operation \`%s\` with sharding table \`%s\`.                                                                     |
| 44000     | 20041       | Can not update sharding value for table \`%s\`.                                                                                  |
| 0A000     | 20042       | The CREATE VIEW statement contains unsupported query statement.                                                                  |
| 44000     | 20043       | PREPARE statement can not support sharding tables route to same data sources.                                                    |
| 44000     | 20044       | The table inserted and the table selected must be the same or bind tables.                                                       |
| 0A000     | 20045       | Can not support DML operation with multiple tables \`%s\`.                                                                       |
| 42000     | 20046       | %s ... LIMIT can not support route to multiple data nodes.                                                                       |
| 44000     | 20047       | Can not find actual data source intersection for logic tables \`%s\`.                                                            |
| 42000     | 20048       | INSERT INTO ... SELECT can not support applying key generator with absent generate key column.                                   |
| 0A000     | 20049       | Alter view rename .. to .. statement should have same config for \`%s\` and \`%s\`.                                              |
| HY000     | 20060       | \`%s %s\` can not route correctly for %s \`%s\`.                                                                                 |
| 42S02     | 20061       | Can not get route result, please check your sharding rule configuration.                                                         |
| 34000     | 20062       | Can not get cursor name from fetch statement.                                                                                    |
| HY000     | 20080       | Sharding algorithm class \`%s\` should be implement \`%s\`.                                                                      |
| HY000     | 20081       | Routed target \`%s\` does not exist, available targets are \`%s\`.                                                               |
| 44000     | 20082       | Inline sharding algorithms expression \`%s\` and sharding column \`%s\` do not match.                                            |
| HY000     | 20083       | Sharding algorithm \`%s\` initialization failed, reason is: %s.                                                                  |
| 44000     | 20084       | Complex inline algorithm need %d sharing columns, but only found %d.                                                             |
| 44000     | 20085       | No sharding database route info.                                                                                                 |
| 44000     | 20086       | Some routed data sources do not belong to configured data sources. routed data sources: \`%s\`, configured data sources: \`%s\`. |
| 44000     | 20087       | Please check your sharding conditions \`%s\` to avoid same record in table \`%s\` routing to multiple data nodes.                |
| 44000     | 20088       | Cannot found routing table factor, data source: %s, actual table: %s.                                                            |
| 44000     | 20090       | Can not find strategy for generate keys with table \`%s\`.                                                                       |
| HY000     | 20091       | Key generate algorithm \`%s\` initialization failed, reason is: %s.                                                              |
| HY000     | 20092       | Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds.                                        |
| HY000     | 20099       | Sharding plugin error, reason is: %s                                                                                             |

### 读写分离

| SQL State | Vendor Code | 错误信息                                                                                        |
|-----------|-------------|---------------------------------------------------------------------------------------------|
| 44000     | 20270       | Inline expression %s names size error.                                                      |
| HY004     | 20280       | Invalid read database weight \`%s\`.                                                        |
| 44000     | 20281       | Load balancer algorithm \`%s\` initialization failed, reason is: \`%s\.`                    |
| 44000     | 20290       | Data source name is required in database \`%s\.`                                            |
| 44000     | 20291       | Write data source name is required in database `\`%s\.`                                     |
| 44000     | 20292       | Read data source names is required in database `\`%s\.`                                     |
| 44000     | 20293       | Can not config duplicate %s data source \`%s\` in database \`%s\.`                          |
| 42S02     | 20294       | %s data source name \`%s\` not in database \`%s\.`                                          |
| 44000     | 20295       | Auto aware data source name is required in database \`%s\.`                                 |
| 42S02     | 20296       | Not found load balance type in database \`%s\.`                                             |
| 44000     | 20297       | Weight load balancer datasource name config does not match data sources in database \`%s\.` |

### SQL 方言转换

| SQL State | Vendor Code | 错误信息                                                |
|-----------|-------------|-----------------------------------------------------|
| 42000     | 20440       | Can not support database \`%s\` in SQL translation. |
| 42000     | 20441       | Translation error, SQL is: %s                       |

### 流量治理

| SQL State | Vendor Code | 错误信息                                |
|-----------|-------------|-------------------------------------|
| 42S02     | 20500       | Can not get traffic execution unit. |

### 数据加密

| SQL State | Vendor Code | 错误信息                                                                                               |
|-----------|-------------|----------------------------------------------------------------------------------------------------|
| 44000     | 20700       | Can not find logic encrypt column by \`%s\`.                                                       |
| 44000     | 20701       | Fail to find encrypt column \`%s\` from table \`%s\`.                                              |
| 44000     | 20702       | Altered column \`%s\` must use same encrypt algorithm with previous column \`%s\` in table \`%s\`. |
| 42000     | 20740       | Insert value of index \`%s\` can not support for encrypt.                                          |
| 0A000     | 20741       | The SQL clause \`%s\` is unsupported in encrypt rule.                                              |
| HY004     | 20780       | Encrypt algorithm \`%s\` initialization failed, reason is: %s.                                     |
| HY004     | 20781       | \`%s\` column's encryptor name \`%s\` does not match encrypt algorithm type \`%s\`.                |
| 44000     | 20703       | Cipher column of \`%s\` can not be null in database \`%s\`.                                        |
| 44000     | 20704       | Can not find (STANDARD\|ASSIST_QUERY\|LIKE_QUERY) encryptor in table \`%s\` and column \`%s\`.     |
| 44000     | 20705       | Assisted query column of \`%s\` can not be null in database \`%s\`.                                |
| 44000     | 20707       | Like query column of \`%s\` can not be null in database \`%s\`.                                    |
| 44000     | 20709       | Can not find encrypt table: \`%s\`.                                                                |
| 44000     | 20710       | Can not found registered encryptor \`%s\` in database \`%s\`.                                      |

### 影子库

| SQL State | Vendor Code | 错误信息                                                                                              |
|-----------|-------------|---------------------------------------------------------------------------------------------------|
| 44000     | 20800       | \`%s\` algorithm does not exist in database \`%s\`.                                               |
| 44000     | 20801       | \`%s\` configuration does not exist in database \`%s\`.                                           |
| 44000     | 20802       | No available shadow data sources mappings in shadow table \`%s\`.                                 |
| 44000     | 20803       | Column shadow algorithm \`%s\` operation only supports one column mapping in shadow table \`%s\`. |
| HY004     | 20820       | Shadow column \`%s\` of table \`%s\` does not support \`%s\` type.                                |
| 42000     | 20840       | Insert value of index \`%s\` can not support for shadow.                                          |
| HY000     | 20880       | Shadow algorithm \`%s\` initialization failed, reason is: %s.                                     |
| 44000     | 20881       | Default shadow algorithm class should be implement HintShadowAlgorithm.                           |

### 数据脱敏

| SQL State | Vendor Code | 错误信息                                                        |
|-----------|-------------|-------------------------------------------------------------|
| HY000     | 20980       | Mask algorithm \`%s\` initialization failed, reason is: %s. |
| 42S02     | 20990       | Invalid mask algorithm \`%s\` in database \`%s\`.           |

## 其他异常

| SQL State | Vendor Code | 错误信息                            |
|-----------|-------------|---------------------------------|
| HY004     | 30000       | Unknown exception: %s           |
| 0A000     | 30001       | Unsupported SQL operation: %s   |
| 0A000     | 30002       | Database protocol exception: %s |
| 0A000     | 30003       | Unsupported command: %s         |
