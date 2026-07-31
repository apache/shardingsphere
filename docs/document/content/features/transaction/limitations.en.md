+++
title = "Limitations"
weight = 2
+++

Although Apache ShardingSphere aims at being compatible with all distributed scenario and providing the best performance, under the CAP theorem guidance, there is no sliver bullet with distributed transaction solution.

The Apache ShardingSphere community chose instead to give the users the ability to choose their preferred distributed transaction type and use the most suitable solution according to their scenarios.

## LOCAL Transaction

### Unsupported

* Does not support the cross-database transactions caused by network or hardware crash. For example, when updating two databases in transaction, if one database crashes before commit, then only the data of the other database can commit.
* When ShardingSphere-Proxy uses PostgreSQL or openGauss, metadata-changing DDL statements such as `CREATE`, `ALTER`, and `DROP` are not supported in transactions.
  Such statements are rejected before execution with SQLSTATE `0A000` to prevent inconsistencies between storage and ShardingSphere metadata after a transaction rollback.

## XA Transaction

### Unsupported

* Recover committing and rolling back in other machines after the service is down.
* MySQL, in the transaction block, the SQL execution is abnormal, and run `Commit`, and data remains consistent.
* After XA transactions are configured, the maximum length of the storage unit name cannot exceed 45 characters.
* PostgreSQL and openGauss have the same metadata-changing DDL restriction described for LOCAL transactions.

## BASE Transaction

### Unsupported

* Does not support isolation level.
