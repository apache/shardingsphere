+++
title = "SQL Error Code"
weight = 1
chapter = true
+++

SQL error codes provide by standard `SQL State`, `Vendor Code` and `Reason`, which return to client when SQL execute error.

**the error codes are draft, still need to be adjusted.**

## Kernel Exception

### Meta data

| Vendor Code | SQL State | Reason                                                                              |
|-------------|-----------|-------------------------------------------------------------------------------------|
| 10000       | 42S02     | Database is required.                                                               |
| 10001       | 42S02     | Schema '%s' does not exist.                                                         |
| 10002       | 42S02     | Table or view '%s' does not exist.                                                  |
| 10003       | 42S02     | Unknown column '%s' in '%s'.                                                        |
| 10100       | HY000     | Can not %s storage units '%s'.                                                      |
| 10101       | 42S02     | There is no storage unit in database '%s'.                                          |
| 10102       | 44000     | Storage units '%s' do not exist in database '%s'.                                   |
| 10103       | 44000     | Storage unit '%s' still used by '%s'.                                               |
| 10104       | 42S01     | Duplicate storage unit names '%s'.                                                  |
| 10110       | 08000     | Storage units can not connect, error messages are: %s.                              |
| 10111       | 0A000     | Can not alter connection info in storage units: '%s'.                               |
| 10120       | 44000     | Invalid storage unit status, error message is: %s.                                  |
| 10200       | 44000     | Invalid '%s' rule '%s', error message is: %s                                        |
| 10201       | 42S02     | There is no rule in database '%s'.                                                  |
| 10202       | 42S02     | %s rules '%s' do not exist in database '%s'.                                        |
| 10203       | 44000     | %s rules '%s' in database '%s' are still in used.                                   |
| 10204       | 42S01     | Duplicate %s rule names '%s' in database '%s'.                                      |
| 13000       | HY004     | Invalid format for actual data node '%s'.                                           |
| 13001       | HY000     | Can not support 3-tier structure for actual data node '%s' with JDBC '%s'.          |
| 10400       | 44000     | Algorithm '%s.'%s' initialization failed, reason is: %s.                            |
| 10401       | 44000     | '%s' algorithm on %s is required.                                                   |
| 10402       | 42S02     | '%s' algorithm '%s' on %s is unregistered.                                          |
| 10403       | 44000     | %s algorithms '%s' in database '%s' are still in used.                              |
| 10404       | 44000     | Invalid %s algorithm configuration '%s'.                                            |
| 10450       | HY000     | Algorithm '%s.%s' execute failed, reason is: %s.                                    |
| 10500       | 44000     | Invalid single rule configuration, reason is: %s.                                   |
| 10501       | 42S02     | Single table '%s' does not exist.                                                   |
| 10502       | HY000     | Can not load table with database name '%s' and data source name '%s', reason is: %s |
| 10503       | 0A000     | Can not drop schema '%s' because of contains tables.                                |

### Data

| Vendor Code | SQL State | Reason                                                |
|-------------|-----------|-------------------------------------------------------|
| 11000       | HY004     | Unsupported conversion data type '%s' for value '%s'. |
| 11001       | HY004     | Unsupported conversion stream charset '%s'.           |

### Syntax

| Vendor Code | SQL State | Reason                                   |
|-------------|-----------|------------------------------------------|
| 12000       | 42000     | SQL String can not be NULL or empty.     |
| 12010       | 42000     | Can not support variable '%s'.           |
| 12011       | HY004     | Invalid variable value '%s'.             |
| 12020       | HV008     | Column index '%d' is out of range.       |
| 12021       | 42S02     | Can not find column label '%s'.          |
| 12022       | HY000     | Column '%s' in %s is ambiguous.          |
| 12100       | 0A000     | DROP TABLE ... CASCADE is not supported. |
| 12100       | 42000     | You have an error in your SQL syntax: %s |
| 12101       | 42000     | Can not accept SQL type '%s'.            |
| 12200       | 42000     | Hint data source '%s' does not exist.    |
| 12201       | 42000     | SQL audit failed, error message: %s.     |

### Connection

| Vendor Code | SQL State | Reason                                                                                                                                                                                                                      |
|-------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 13000       | 0A000     | Unsupported storage type of URL '%s'.                                                                                                                                                                                       |
| 13001       | 08000     | The URL '%s' is not recognized, please refer to the pattern '%s'.                                                                                                                                                           |
| 13010       | 08000     | Can not get %d connections one time, partition succeed connection(%d) have released. Please consider increasing the \`maxPoolSize\` of the data sources or decreasing the \`max-connections-size-per-query\` in properties. |
| 13011       | 08000     | SQL execution has been interrupted.                                                                                                                                                                                         |
| 13030       | 01000     | Circuit break open, the request has been ignored.                                                                                                                                                                           |
| 13040       | HY000     | Load datetime from database failed, reason: %s                                                                                                                                                                              |
| 13100       | 08000     | Can not register driver.                                                                                                                                                                                                    |
| 13101       | 08000     | Connection has been closed.                                                                                                                                                                                                 |
| 13102       | 08000     | Result set has been closed.                                                                                                                                                                                                 |

### Transaction

| Vendor Code | SQL State | Reason                                                                             |
|-------------|-----------|------------------------------------------------------------------------------------|
| 14000       | 25000     | Switch transaction type failed, please terminate the current transaction.          |
| 14001       | 25000     | Can not find transaction manager of \`%s\`.                                        |
| 14002       | 25000     | Transaction timeout should more than 0s.                                           |
| 14200       | 25000     | Can not start new XA transaction in a active transaction.                          |
| 14201       | 25000     | Failed to create \`%s\` XA data source.                                            |
| 14202       | 25000     | Max length of xa unique resource name \`%s\` exceeded: should be less than 45.     |
| 14203       | 25000     | Check privileges failed on data source, reason is: \`%s\`                          |
| 14204       | 25000     | Failed to create XA transaction manager, requires \`%s\` privileges                |
| 14205       | 25000     | Close transaction manager failed, \`%s\`                                           |
| 14301       | 25000     | ShardingSphere Seata-AT transaction has been disabled.                             |
| 14302       | 25000     | Please config application id within seata.conf file.                               |

### Cluster

| Vendor Code | SQL State | Reason                                                        |
|-------------|-----------|---------------------------------------------------------------|
| 17000       | 44000     | Mode must be 'cluster'.                                       |
| 17001       | HY000     | Work ID assigned failed, which can not exceed 1024.           |
| 17002       | HY000     | File access failed, file is: %s                               |
| 17010       | HY000     | Cluster persist repository error, reason is: %s               |
| 17020       | HY000     | The cluster status is %s, can not support SQL statement '%s'. |

### Migration

| Vendor Code | SQL State | Reason                                                                            |
|-------------|-----------|-----------------------------------------------------------------------------------|
| 18002       | 42S02     | There is no rule in database \`%s\`.                                              |
| 18003       | 44000     | Mode configuration does not exist.                                                |
| 18004       | 44000     | Target database \`%s\` isn't exist.                                               |
| 18005       | 22023     | There is invalid parameter value: \`%s\`.                                         |
| 18020       | HY000     | Failed to get DDL for table \`%s\`.                                               |
| 18030       | 42S01     | Duplicate storage unit names \`%s\`.                                              |
| 18031       | 42S02     | Storage units names \`%s\` do not exist.                                          |
| 18050       | HY000     | Before data record is \`%s\`, after data record is \`%s\`.                        |
| 18051       | 08000     | Data check table \`%s\` failed.                                                   |
| 18052       | 0A000     | Unsupported pipeline database type \`%s\`.                                        |
| 18053       | 0A000     | Unsupported CRC32 data consistency calculate algorithm with database type \`%s\`. |
| 18054       | 0A000     | Unsupported mode type \`%s\`.                                                     |
| 18080       | HY000     | Can not find pipeline job \`%s\`.                                                 |
| 18082       | HY000     | Sharding count of job \`%s\` is 0.                                                |
| 18083       | HY000     | Can not split by range for table \`%s\`, reason is: %s                            |
| 18084       | HY000     | Can not split by unique key \`%s\` for table \`%s\`, reason is: %s                |
| 18085       | HY000     | Target table \`%s\` is not empty.                                                 |
| 18086       | 01007     | Source data source lacks %s privilege(s).                                         |
| 18087       | HY000     | Source data source required \`%s = %s\`, now is \`%s\`.                           |
| 18088       | HY000     | User \`%s\` does exist.                                                           |
| 18089       | 08000     | Check privileges failed on source data source, reason is: %s                      |
| 18090       | 08000     | Data sources can not connect, reason is: %s                                       |
| 18091       | HY000     | Importer job write data failed.                                                   |
| 18092       | 08000     | Get binlog position failed by job \`%s\`, reason is: %s                           |
| 18095       | HY000     | Can not find consistency check job of \`%s\`.                                     |
| 18096       | HY000     | Uncompleted consistency check job \`%s\` exists.                                  |
| 18200       | HY000     | Not find stream data source table.                                                |
| 18201       | HY000     | CDC server exception, reason is: %s.                                              |
| 18202       | HY000     | CDC login failed, reason is: %s                                                   |

## Feature Exception

### Data Sharding

| Vendor Code | SQL State | Reason                                                                                                                           |
|-------------|-----------|----------------------------------------------------------------------------------------------------------------------------------|
| 20000       | 44000     | Can not find table rule with logic tables \`%s\`.                                                                                |
| 20001       | 44000     | Can not get uniformed table structure for logic table \`%s\`, it has different meta data of actual tables are as follows: %s     |
| 20002       | 42S02     | Can not find data source in sharding rule, invalid actual data node \`%s\`.                                                      |
| 20003       | 44000     | Data nodes must be configured for sharding table \`%s\`.                                                                         |
| 20004       | 44000     | Actual table \`%s.%s\` is not in table rule configuration.                                                                       |
| 20005       | 44000     | Can not find binding actual table, data source is \`%s\`, logic table is \`%s\`, other actual table is \`%s\`.                   |
| 20006       | 44000     | Actual tables \`%s\` are in use.                                                                                                 |
| 20007       | 42S01     | Index \`%s\` already exists.                                                                                                     |
| 20008       | 42S02     | Index \`%s\` does not exist.                                                                                                     |
| 20009       | 42S01     | View name has to bind to %s tables.                                                                                              |
| 20011       | 44000     | \`%s\` configuration does not exist in database \`%s\`.                                                                          |
| 20012       | 44000     | Invalid binding table configuration in ShardingRuleConfiguration.                                                                |
| 20013       | 44000     | Can not find sharding rule.                                                                                                      |
| 20014       | 44000     | Only allowed 0 or 1 sharding strategy configuration.                                                                             |
| 20020       | 44000     | Sharding value can't be null in sql statement.                                                                                   |
| 20021       | HY004     | Found different types for sharding value \`%s\`.                                                                                 |
| 20022       | HY004     | Invalid %s, datetime pattern should be \`%s\`, value is \`%s\`.                                                                  |
| 20023       | 44000     | Sharding value %s subtract stop offset %d can not be less than start offset %d.                                                  |
| 20024       | 44000     | %s value \`%s\` must implements Comparable.                                                                                      |
| 20040       | 0A000     | Can not support operation \`%s\` with sharding table \`%s\`.                                                                     |
| 20041       | 44000     | Can not update sharding value for table \`%s\`.                                                                                  |
| 20042       | 0A000     | The CREATE VIEW statement contains unsupported query statement.                                                                  |
| 20043       | 44000     | PREPARE statement can not support sharding tables route to same data sources.                                                    |
| 20044       | 44000     | The table inserted and the table selected must be the same or bind tables.                                                       |
| 20045       | 0A000     | Can not support DML operation with multiple tables \`%s\`.                                                                       |
| 20046       | 42000     | %s ... LIMIT can not support route to multiple data nodes.                                                                       |
| 20047       | 44000     | Can not find actual data source intersection for logic tables \`%s\`.                                                            |
| 20048       | 42000     | INSERT INTO ... SELECT can not support applying key generator with absent generate key column.                                   |
| 20049       | 0A000     | Alter view rename .. to .. statement should have same config for \`%s\` and \`%s\`.                                              |
| 20060       | HY000     | \`%s %s\` can not route correctly for %s \`%s\`.                                                                                 |
| 20061       | 42S02     | Can not get route result, please check your sharding rule configuration.                                                         |
| 20062       | 34000     | Can not get cursor name from fetch statement.                                                                                    |
| 20080       | HY000     | Sharding algorithm class \`%s\` should be implement \`%s\`.                                                                      |
| 20081       | HY000     | Routed target \`%s\` does not exist, available targets are \`%s\`.                                                               |
| 20082       | 44000     | Inline sharding algorithms expression \`%s\` and sharding column \`%s\` do not match.                                            |
| 20084       | 44000     | Complex inline algorithm need %d sharing columns, but only found %d.                                                             |
| 20085       | 44000     | No sharding database route info.                                                                                                 |
| 20086       | 44000     | Some routed data sources do not belong to configured data sources. routed data sources: \`%s\`, configured data sources: \`%s\`. |
| 20087       | 44000     | Please check your sharding conditions \`%s\` to avoid same record in table \`%s\` routing to multiple data nodes.                |
| 20088       | 44000     | Cannot found routing table factor, data source: %s, actual table: %s.                                                            |
| 20090       | 42000     | Sharding SQL audit failed, error message: %s.                                                                                    |

### Readwrite-splitting

| Vendor Code | SQL State | Reason                                                             |
|-------------|-----------|--------------------------------------------------------------------|
| 20270       | 44000     | Inline expression %s names size error.                             |
| 20280       | HY004     | Invalid read database weight \`%s\`.                               |
| 20290       | 44000     | Data source name is required in database \`%s\.`                   |
| 20291       | 44000     | Write data source name is required in database `\`%s\.`            |
| 20292       | 44000     | Read data source names is required in database `\`%s\.`            |
| 20293       | 44000     | Can not config duplicate %s data source \`%s\` in database \`%s\.` |
| 20294       | 42S02     | %s data source name \`%s\` not in database \`%s\.`                 |
| 20295       | 44000     | Auto aware data source name is required in database \`%s\.`        |
| 20296       | 44000     | Read storage unit '%s' does not exist in rule '%s'.                |

### SQL Dialect Translator

| Vendor Code | SQL State | Reason                                              |
|-------------|-----------|-----------------------------------------------------|
| 20440       | 42000     | Can not support database \`%s\` in SQL translation. |
| 20441       | 42000     | Translation error, SQL is: %s                       |

### Traffic Management

| Vendor Code | SQL State | Reason                              |
|-------------|-----------|-------------------------------------|
| 20500       | 42S02     | Can not get traffic execution unit. |

### Data Encrypt

| Vendor Code | SQL State | Reason                                                                                                |
|-------------|-----------|-------------------------------------------------------------------------------------------------------|
| 20700       | 44000     | Can not find logic encrypt column by \`%s\`.                                                          |
| 20701       | 44000     | Fail to find encrypt column \`%s\` from table \`%s\`.                                                 |
| 20702       | 44000     | Altered column \`%s\` must use same encrypt algorithm with previous column \`%s\` in table \`%s\`.    |
| 20740       | 42000     | Insert value of index \`%s\` can not support for encrypt.                                             |
| 20741       | 0A000     | The SQL clause \`%s\` is unsupported in encrypt rule.                                                 |
| 20781       | HY004     | \`%s\` column's encryptor name \`%s\` does not match encrypt algorithm type \`%s\ in database \`%s\`. |
| 20703       | 44000     | Cipher column of \`%s\` can not be null in database \`%s\`.                                           |
| 20704       | 44000     | Can not find (STANDARD\|ASSIST_QUERY\|LIKE_QUERY) encryptor in table \`%s\` and column \`%s\`.        |
| 20705       | 44000     | Assisted query column of \`%s\` can not be null in database \`%s\`.                                   |
| 20707       | 44000     | Like query column of \`%s\` can not be null in database \`%s\`.                                       |
| 20709       | 44000     | Can not find encrypt table: \`%s\`.                                                                   |

### Shadow Database

| Vendor Code | SQL State | Reason                                                                                            |
|-------------|-----------|---------------------------------------------------------------------------------------------------|
| 20801       | 44000     | \`%s\` configuration does not exist in database \`%s\`.                                           |
| 20802       | 44000     | No available shadow data sources mappings in shadow table \`%s\`.                                 |
| 20803       | 44000     | Column shadow algorithm \`%s\` operation only supports one column mapping in shadow table \`%s\`. |
| 20820       | HY004     | Shadow column \`%s\` of table \`%s\` does not support \`%s\` type.                                |
| 20840       | 42000     | Insert value of index \`%s\` can not support for shadow.                                          |
| 20881       | 44000     | Default shadow algorithm class should be implement HintShadowAlgorithm.                           |

### SQL Federation

| Vendor Code | SQL State | Reason                                                    |
|-------------|-----------|-----------------------------------------------------------|
| 22040       | 42000     | Unsupported SQL node conversion for SQL statement \`%s\`. |
| 22041       | 42000     | SQL federation doesn't support SQL \`%s\` execution.      |

## Other Exception

| Vendor Code | SQL State | Reason                          |
|-------------|-----------|---------------------------------|
| 30000       | HY000     | Unknown exception: %s           |
| 30001       | 0A000     | Unsupported SQL operation: %s   |
| 30002       | 0A000     | Database protocol exception: %s |
| 30003       | 0A000     | Unsupported command: %s         |
| 30004       | HY000     | Server exception: %s            |
| 30010       | HY000     | Can not find plugin class '%s'. |
