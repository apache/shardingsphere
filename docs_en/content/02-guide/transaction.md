+++
toc = true
title = "Transaction Support"
weight = 9
prev = "/02-guide/key-generator/"
next = "/02-guide/subquery/"

+++

Considering the performance, sharding-jdbc decides not to support strong consistency distributed transactions. In the future, it will support the B.A.S.E transaction which makes the final result of all the distributed databases consistent. Currently, in addition to supporting weak XA transactions, we have been able to provide the Best-Effort-Delivery transaction, one of the B.A.S.E transaction.

Notices:

* Support the none-cross-database transactions, e.g. table sharding without database sharding, or database sharding with the queries routed in the same database.

* Support the exception handling for the cross-database transactions due to logical exceptions. For example, in the same transaction, you want to update two databases, Sharding-JDBC will rollback all transactions for all two database when a null pointer is thrown after updating.

* Do not support the exception handling for the cross-database transactions due to network or hardware exceptions. For example, in the same transaction, you want to update two databases, Sharding-JDBC will only commit the transaction for second database when the first database is dead after updating.

# The B.A.S.E transaction

## The Best-Effort-Delivery transaction

### The concept

For the distributed databases, we believe the operations for all the databases will succeed eventually, so the system will keep on trying to send the operations to its corresponding database.

### The structure diagram 
![The Best-Effort-Delivery transaction](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture-soft-transaction-bed.png)

### The usage scenario

* To delete records by the primary key.
* Permanently update the record's status, e.g. to update notification service status.

### The usage limit

It is necessary to satisfy the idempotent requirement when the B.A.S.E transaction is used.

* The INSERT statement must contain the column of primary key which can not be auto_increment.
* The UPDATE statement must satisfy the idempotent requirement, and UPDATE xxx SET x=x+1 is not supported.
* All the DELETE statements are supported。

### The develop guide

* For the sharding-jdbc-transaction is developed by using JAVA and can be directly used in the form of jar package, so that you can use the Maven to import coordinates to use it.
* In order to ensure that the transactions are not lost, sharding-jdbc-transaction needs to store the log of all the transactions, which can be configured in the transaction manager configurations.
* You need to deploy discrete jobs and Zookeeper because of the asynchronous way of the B.A.S.E transactions. To simply those operations, we develop sharding-jdbc-transaction-async-job for sharding-jdbc-transaction, so you can create the high-available jobs to asynchronously deliver the B.A.S.E transactions. The startup script is start.sh.
* To help users develop easily, sharding-jdb-transaction provides memory-based transaction log storage and embedded asynchronous jobs.

### The develop example

```java
    // 1. To configure SoftTransactionConfiguration
    SoftTransactionConfiguration transactionConfig = new SoftTransactionConfiguration(dataSource);
    transactionConfig.setXXX();
    
    // 2. To initialize SoftTransactionManager
    SoftTransactionManager transactionManager = new SoftTransactionManager(transactionConfig);
    transactionManager.init();
    
    // 3. To get BEDSoftTransaction
    BEDSoftTransaction transaction = (BEDSoftTransaction) transactionManager.getTransaction(SoftTransactionType.BestEffortsDelivery);
    
    // 4. To start a transaction
    transaction.begin(connection);
    
    // 5. To execute JDBC
    /* 
        codes here
    */
    * 
    // 6. To close the connection
    transaction.end();
```

### The configuration of transaction manager 

### SoftTransactionConfiguration Configuration
For configuring transaction manager.

| *Name*                              | *Type*                                     | *Required* | *Default*   | *Info*                                                                                       |
| ---------------------------------- | ------------------------------------------ | ------ | --------- | ------------------------------------------------------------------------------------------- |
| shardingDataSource                 | ShardingDataSource                         | Y     |           | The data source of transaction manager                                                                         |
| syncMaxDeliveryTryTimes            | int                                        | N     | 3         | The maximum number of attempts to send transactions.                                                                 |
| storageType                        | enum                                       | N     | RDB       | The storage type of transaction logs, The options are RDB(creating tables automatically) or MEMORY.                                       |
| transactionLogDataSource           | DataSource                                 | N     | null      | The data source to store the transaction log. if storageType is RDB, this item is required.                                              |
| bestEffortsDeliveryJobConfiguration| NestedBestEffortsDeliveryJobConfiguration  | N     | null      | The config of embedded asynchronous jobs for the Best-Effort-Delivery transaction, please refer to NestedBestEffortsDeliveryJobConfiguration.|

### NestedBestEffortsDeliveryJobConfiguration Configuration (Only for developing environment)

It is for configuring embedded asynchronous jobs for development environment only. The production environment should adopt the deployed discrete jobs.

| *Name*                              | *Type*                                     | *Required* | *Default*   | *Info*                                                            |
| ---------------------------------- | --------------------------- | ------ | ------------------------ | --------------------------------------------------------------- |
| zookeeperPort                      | int                         | N     | 4181                     | The port of the embedded registry.                                               |
| zookeeperDataDir                   | String                      | N     | target/test_zk_data/nano/| The data directory of the embedded registry.                                      |
| asyncMaxDeliveryTryTimes           | int                         | N     | 3                        | The maximum number of attempts to send transactions asynchronously.                                       |
| asyncMaxDeliveryTryDelayMillis     | long                        | N     | 60000                    | The number of delayed milliseconds to execute asynchronous transactions. The transactions whose creating time earlier than this value will be executed by asynchronous jobs.  |

### The operations to deploy the discrete jobs

* Create database to store transactions logs.
* Deploy Zookeeper for asynchronous jobs.
* Configure YAML.
* Download and extract sharding-jdbc-transaction-async-job-$VERSION.tar, and start asynchronous jobs by running start.sh.

### The YAML configuration of asynchronous jobs
```yaml
# The target data source.
targetDataSource:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password:
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:

# The data source of transaction logs.
transactionLogDataSource:
  ds_trans: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/trans_log
    username: root
    password:

# The registry configuration
zkConfig:
  # The url of the registry
  connectionString: localhost:2181
  
  # The namespace of jobs
  namespace: Best-Efforts-Delivery-Job
  
  # The inital value of the retry interval to connect to the registry.
  baseSleepTimeMilliseconds: 1000
  
  # The max value of the retry interval to connect to the registry.
  maxSleepTimeMilliseconds: 3000
  
  # The max number of retry to connect to the registry.
  maxRetries: 3

# The job configuration
jobConfig:
  # The job name
  name: bestEffortsDeliveryJob
  
  # The cron expression to trigger jobs
  cron: 0/5 * * * * ?
  
  # The max number of transaction logs for each assignment.
  transactionLogFetchDataCount: 100
  
  # The max number of retry to send the transactions.
  maxDeliveryTryTimes: 3
  
  # The number of delayed milliseconds to execute asynchronous transactions. The transactions whose creating time earlier than this value will be executed by asynchronous jobs.
  maxDeliveryTryDelayMillis: 60000
```

## The TCC transaction
Waiting...

