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
| 10004       | 42S02     | Index '%s' does not exist.                                                          |
| 10005       | 42S01     | Index '%s' already exists.                                                          |
| 10010       | HY000     | Rule and storage meta data mismatched, reason is: %s.                               |
| 10100       | HY000     | Can not %s storage units '%s'.                                                      |
| 10012       | HY000     | Load table meta data failed for database '%s' and tables '%s'.                      |
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
| 10210       | 42S02     | %s strategies '%s' do not exist.                                                    |
| 10300       | HY000     | Invalid format for actual data node '%s'.                                           |
| 10301       | 0A000     | Can not support 3-tier structure for actual data node '%s' with JDBC '%s'.          |
| 10400       | 44000     | Algorithm '%s' initialization failed, reason is: %s.                                |
| 10401       | 42S02     | '%s' algorithm on %s is required.                                                   |
| 10402       | 42S02     | '%s' algorithm '%s' on %s is unregistered.                                          |
| 10403       | 44000     | %s algorithms '%s' in database '%s' are still in used.                              |
| 10404       | 44000     | Invalid %s algorithm configuration '%s'.                                            |
| 10410       | 0A000     | Unsupported %s.%s with database type '%s'.                                          |
| 10440       | HY000     | Algorithm '%s' execute failed, reason is: %s.                                       |
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

| Vendor Code | SQL State | Reason                                                                                                                           |
|-------------|-----------|----------------------------------------------------------------------------------------------------------------------------------|
| 12000       | 42000     | SQL String can not be NULL or empty.                                                                                             |
| 12010       | 44000     | Can not support variable '%s'.                                                                                                   |
| 12011       | HY004     | Invalid variable value '%s'.                                                                                                     |
| 12020       | HV008     | Column index '%d' is out of range.                                                                                               |
| 12021       | 42S02     | Can not find column label '%s'.                                                                                                  |
| 12022       | HY000     | Column '%s' in %s is ambiguous.                                                                                                  |
| 12100       | 42000     | You have an error in your SQL syntax: %s                                                                                         |
| 12101       | 42000     | Can not accept SQL type '%s'.                                                                                                    |
| 12200       | 42000     | Hint data source '%s' does not exist.                                                                                            |
| 12300       | 0A000     | DROP TABLE ... CASCADE is not supported.                                                                                         |
| 12500       | 42000     | Not unique table/alias: '%s'.                                                                                                    |
| 12600       | HY000     | In definition of view, derived table or common table expression, SELECT list and column names list have different column counts. |

### Connection

| Vendor Code | SQL State | Reason                                                                                                                                                                                                                  |
|-------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 13000       | 08000     | Can not get %d connections one time, partition succeed connection(%d) have released. Please consider increasing the 'maxPoolSize' of the data sources or decreasing the 'max-connections-size-per-query' in properties. |
| 13001       | 08000     | SQL execution has been interrupted.                                                                                                                                                                                     |
| 13010       | 01000     | Circuit break open, the request has been ignored.                                                                                                                                                                       |
| 13100       | 0A000     | Unsupported storage type of URL '%s'.                                                                                                                                                                                   |
| 13101       | 08000     | The URL '%s' is not recognized, please refer to the pattern '%s'.                                                                                                                                                       |
| 13200       | 08000     | Can not register driver.                                                                                                                                                                                                |
| 13201       | 08000     | Connection has been closed.                                                                                                                                                                                             |
| 13202       | 08000     | Result set has been closed.                                                                                                                                                                                             |
| 13400       | HY000     | Load datetime from database failed, reason: %s                                                                                                                                                                          |

### Transaction

| Vendor Code | SQL State | Reason                                                                                    |
|-------------|-----------|-------------------------------------------------------------------------------------------|
| 14000       | 25000     | Switch transaction type failed, please terminate the current transaction.                 |
| 14001       | 42S02     | Can not find transaction manager of '%s'.                                                 |
| 14002       | 44000     | Max length of unique resource name '%s' exceeded, should be less than 45.                 |
| 14003       | 25000     | Transaction timeout should more than 0.                                                   |
| 14004       | 25000     | Close transaction manager failed.                                                         |
| 14200       | 25000     | Failed to create '%s' XA data source.                                                     |
| 14201       | 25000     | Can not start new XA transaction in a active transaction.                                 |
| 14202       | 25000     | Check XA transaction privileges failed on data source, please grant '%s' to current user. |
| 14400       | 44000     | No application id within 'seata.conf' file.                                               |
| 14401       | 25000     | Seata-AT transaction has been disabled.                                                   |

### Lock

| Vendor Code | SQL State | Reason                     |
|-------------|-----------|----------------------------|
| 15030       | HY000     | Cluster is already locked. |
| 15031       | HY000     | Cluster is not locked.     |

### Cluster

| Vendor Code | SQL State | Reason                                                        |
|-------------|-----------|---------------------------------------------------------------|
| 17000       | 44000     | Mode must be 'cluster'.                                       |
| 17001       | HY000     | Worker ID assigned failed, which should be in [0, %s).        |
| 17010       | HY000     | Cluster persist repository error, reason is: %s               |
| 17011       | HY000     | Failed to reload meta data context.                           |
| 17020       | HY000     | The cluster status is %s, can not support SQL statement '%s'. |
| 17100       | 42S02     | Cluster persist repository configuration is required.         |

### Data Pipeline

| Vendor Code | SQL State | Reason                                                                         |
|-------------|-----------|--------------------------------------------------------------------------------|
| 18000       | 22023     | There is invalid parameter value '%s'.                                         |
| 18100       | 42S02     | Target database '%s' does not exist.                                           |
| 18101       | 42S02     | Can not find pipeline job '%s'.                                                |
| 18102       | 44000     | Sharding count of job '%s' is 0.                                               |
| 18103       | 42S02     | Can not get meta data for table '%s' when split by range.                      |
| 18104       | HY000     | Can not split by unique key '%s' for table '%s'.                               |
| 18105       | HY000     | Target table '%s' is not empty.                                                |
| 18108       | 42S02     | User '%s' does exist.                                                          |
| 18110       | HY000     | Importer job write data failed.                                                |
| 18111       | 08000     | Get binlog position failed by job '%s'.                                        |
| 18112       | HY000     | Can not find consistency check job of '%s'.                                    |
| 18113       | HY000     | Uncompleted consistency check job '%s' exists, progress '%s'.                  |
| 18114       | HY000     | Failed to get DDL for table '%s'.                                              |
| 18200       | HY000     | Before data record is '%s', after data record is '%s'.                         |
| 18201       | 08000     | Data check table '%s' failed.                                                  |
| 18202       | 0A000     | Unsupported pipeline database type '%s'.                                       |
| 18400       | 42S02     | Can not find stream data source table.                                         |
| 18401       | 42S02     | Database '%s' does not exist.                                                  |
| 18410       | 42S02     | CDC Login request body is empty.                                               |
| 18411       | 08004     | Illegal username or password.                                                  |

## Feature Exception

### Data Sharding

| Vendor Code | SQL State | Reason                                                                                                                              |
|-------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------|
| 20000       | 42S02     | %s configuration does not exist in database '%s'.                                                                                   |
| 20001       | 42S02     | Can not find table rule with logic tables '%s'.                                                                                     |
| 20002       | 42S02     | Can not find data source in sharding rule, invalid actual data node '%s'.                                                           |
| 20003       | 42S02     | Data nodes is required for sharding table '%s'.                                                                                     |
| 20004       | 42S02     | Actual table '%s.%s' is not in table rule configuration.                                                                            |
| 20005       | 42S02     | Can not find binding actual table, data source is '%s', logic table is '%s', other actual table is '%s'.                            |
| 20006       | 44000     | Actual tables '%s' are in use.                                                                                                      |
| 20009       | 42S01     | View name has to bind to %s tables.                                                                                                 |
| 20010       | 44000     | Invalid binding table configuration.                                                                                                |
| 20011       | 44000     | Only allowed 0 or 1 sharding strategy configuration.                                                                                |
| 20012       | 42S01     | Same actual data node cannot be configured in multiple logic tables in same database, logical table '%s', actual data node '%s.%s'. |
| 20020       | 44000     | Sharding value can not be null in SQL statement.                                                                                    |
| 20021       | HY004     | Found different types for sharding value '%s'.                                                                                      |
| 20022       | HY004     | Invalid %s, datetime pattern should be '%s', value is '%s'.                                                                         |
| 20023       | 44000     | Sharding value %s subtract stop offset %d can not be less than start offset %d.                                                     |
| 20024       | 44000     | %s value '%s' must implements Comparable.                                                                                           |
| 20030       | 0A000     | Can not support operation '%s' with sharding table '%s'.                                                                            |
| 20031       | 44000     | Can not update sharding value for table '%s'.                                                                                       |
| 20032       | 0A000     | The CREATE VIEW statement contains unsupported query statement.                                                                     |
| 20033       | 44000     | PREPARE statement can not support sharding tables route to same data sources.                                                       |
| 20034       | 44000     | The table inserted and the table selected must be the same or bind tables.                                                          |
| 20035       | 0A000     | Can not support DML operation with multiple tables '%s'.                                                                            |
| 20036       | 42000     | %s ... LIMIT can not support route to multiple data nodes.                                                                          |
| 20037       | 44000     | Can not find actual data source intersection for logic tables '%s'.                                                                 |
| 20038       | 42000     | INSERT INTO ... SELECT can not support applying key generator with absent generate key column.                                      |
| 20039       | 0A000     | Alter view rename .. to .. statement should have same config for '%s' and '%s'.                                                     |
| 20040       | HY000     | '%s %s' can not route correctly for %s '%s'.                                                                                        |
| 20041       | 42S02     | Can not get route result, please check your sharding rule configuration.                                                            |
| 20042       | 34000     | Can not get cursor name from fetch statement.                                                                                       |
| 20050       | HY000     | Sharding algorithm class '%s' should be implement '%s'.                                                                             |
| 20051       | HY000     | Routed target '%s' does not exist, available targets are '%s'.                                                                      |
| 20052       | 44000     | Inline sharding algorithms expression '%s' and sharding column '%s' do not match.                                                   |
| 20053       | 44000     | Complex inline algorithm need %d sharding columns, but only found %d.                                                               |
| 20054       | 44000     | No sharding database route info, actual data source names: `%s`, sharding condition values: `%s`.                                   |
| 20055       | 44000     | Some routed data sources do not belong to configured data sources. routed data sources '%s', configured data sources '%s', sharding condition values '%s'.          |
| 20056       | 44000     | Please check your sharding conditions '%s' to avoid same record in table '%s' routing to multiple data nodes.                       |
| 20057       | 44000     | Can not find routing table factor, data source '%s', actual table '%s'.                                                             |
| 20060       | HY000     | Invalid %s strategy '%s', strategy does not match data nodes.                                                                       |
| 20090       | 42000     | Not allow DML operation without sharding conditions.                                                                                |

### SQL Federation

| Vendor Code | SQL State | Reason                                                  |
|-------------|-----------|---------------------------------------------------------|
| 20100       | 42000     | Unsupported SQL node conversion for SQL statement '%s'. |
| 20101       | 42000     | SQL federation does not support SQL '%s'.               |
| 20102       | 42S02     | SQL federation schema '%s' not found in SQL '%s'.       |

### Readwrite-splitting

| Vendor Code | SQL State | Reason                                                                      |
|-------------|-----------|-----------------------------------------------------------------------------|
| 20200       | 42S02     | Readwrite-splitting data source rule name is required in database '%s'.     |
| 20201       | 42S02     | Can not find readwrite-splitting data source rule '%s' in database '%s'.    |
| 20202       | 42S02     | Readwrite-splitting [READ/WRITE] data source is required in %s.             |
| 20203       | 42S02     | Can not find readwrite-splitting [READ/WRITE] data source '%s' in %s.       |
| 20204       | 42S01     | Readwrite-splitting [READ/WRITE] data source '%s' is duplicated in %s.      |
| 20205       | 44000     | Readwrite-splitting [READ/WRITE] data source inline expression error in %s. |

### SQL Dialect Translator

| Vendor Code | SQL State | Reason                                            |
|-------------|-----------|---------------------------------------------------|
| 20400       | 0A000     | Can not support database '%s' in SQL translation. |

### Traffic Management

| Vendor Code | SQL State | Reason                              |
|-------------|-----------|-------------------------------------|
| 20500       | 42S02     | Can not get traffic execution unit. |

### Data Encrypt

| Vendor Code | SQL State | Reason                                                                                       |
|-------------|-----------|----------------------------------------------------------------------------------------------|
| 21000       | 42S02     | %s column is required in %s.                                                                 |
| 21001       | 42S02     | Can not find encrypt table '%s'.                                                             |
| 21002       | 42S02     | Can not find logic encrypt column by '%s'.                                                   |
| 21003       | 42S02     | Can not find encrypt column '%s' from table '%s'.                                            |
| 21004       | HY000     | '%s' column's encrypt algorithm '%s' should support %s in database '%s'.                     |
| 21005       | HY000     | Column '%s' of table '%s' is not configured with %s query algorithm.                         |
| 21010       | 44000     | Altered column '%s' must use same encrypt algorithm with previous column '%s' in table '%s'. |
| 21020       | 0A000     | The SQL clause '%s' is unsupported in encrypt feature.                                       |
| 21030       | 22000     | Failed to decrypt the ciphertext '%s' in the column '%s' of table '%s'.                      |


### Shadow Database

| Vendor Code | SQL State | Reason                                                                  |
|-------------|-----------|-------------------------------------------------------------------------|
| 22000       | 42S02     | Production data source configuration does not exist in database '%s'.   |
| 22001       | 42S02     | Shadow data source configuration does not exist in database '%s'.       |
| 22002       | 42S02     | No available shadow data sources mappings in shadow table '%s'.         |
| 22003       | 44000     | Default shadow algorithm class should be implement HintShadowAlgorithm. |
| 22010       | HY004     | Shadow column '%s' of table '%s' does not support '%s' type.            |
| 22020       | 42000     | Insert value of index '%d' can not support for shadow.                  |

## Other Exception

| Vendor Code | SQL State | Reason                                               |
|-------------|-----------|------------------------------------------------------|
| 30000       | HY000     | Unknown exception: %s                                |
| 30001       | 0A000     | Unsupported SQL operation: %s                        |
| 30002       | HY000     | Database protocol exception: %s                      |
| 30003       | 0A000     | Unsupported command: %s                              |
| 30004       | HY000     | Server exception: %s                                 |
| 30005       | HY000     | Underlying SQL state: %s, underlying error code: %s. |
| 30010       | HY000     | Can not find plugin class '%s'.                      |
| 30020       | HY000     | File access failed, file is: %s                      |
| 30030       | HY000     | Unexpected tableless route engine.                   |
