+++
title = "Limitations"
weight = 2
+++

Though Apache ShardingSphere intends to be compatible with all distributed scenario and best performance, under CAP theorem guidance, there is no sliver bullet with distributed transaction solution.

Apache ShardingSphere wants to give the user choice of distributed transaction type and use the most suitable solution in different scenarios.

## LOCAL Transaction

### Supported

* Support none-cross-database transactions. For example, sharding table or sharding database with its route result in same database;
* Support cross-database transactions caused by logic exceptions. For example, update two databases in transaction with exception thrown, data can rollback in both databases.

### Unsupported

* Do not support the cross-database transactions caused by network or hardware crash. For example, when update two databases in transaction, if one database crashes before commit, then only the data of the other database can commit.

## XA Transaction

### Supported

* Support Savepoint;
* PostgreSQL/OpenGauss, in the transaction block, the SQL execution is abnormal，then run `Commit`，transactions are automatically rollback;
* Support cross-database transactions after sharding;
* Operation atomicity and high data consistency in 2PC transactions;
* When service is down and restarted, commit and rollback transactions can be recovered automatically;
* Support use XA and non-XA connection pool together;
* Support transactions across multiple logical databases.

### Unsupported

* Recover committing and rolling back in other machines after the service is down;
* MySQL,in the transaction block, the SQL execution is abnormal, and run `Commit`, and data remains consistent.

## BASE Transaction

### Supported

* Support cross-database transactions after sharding;
* Rollback transaction according to undo log;
* Support recovery committing transaction automatically after the service is down.

### Unsupported

* Do not support isolation level.
