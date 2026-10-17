+++
title = "Limitations"
weight = 2
+++

Although Apache ShardingSphere aims at being compatible with all distributed scenario and providing the best performance, under the CAP theorem guidance, there is no sliver bullet with distributed transaction solution.

The Apache ShardingSphere community chose instead to give the users the ability to choose their preferred distributed transaction type and use the most suitable solution according to their scenarios.

## LOCAL Transaction

### Unsupported

* Does not support the cross-database transactions caused by network or hardware crash. For example, when updating two databases in transaction, if one database crashes before commit, then only the data of the other database can commit.
* When ShardingSphere-Proxy uses PostgreSQL, only `CREATE TABLE`, `ALTER TABLE`, `DROP TABLE` and `RENAME TABLE` are supported in transactions, and their metadata refresh is deferred until the transaction ends.
  Every other metadata-changing DDL statement, including schema, view and index statements, is not supported. When ShardingSphere-Proxy uses openGauss, no metadata-changing DDL statement is supported in transactions.
  Unsupported statements are rejected before execution with SQLSTATE `0A000` to prevent inconsistencies between storage and ShardingSphere metadata after a transaction rollback.
* A statement cannot reference a table created earlier in the same transaction, because the meta data of that table is only applied when the transaction ends.
  For example `BEGIN; CREATE TABLE t (...); ALTER TABLE t ...;` fails on the second statement.
* The table DDL support above applies only when every storage unit the statement is routed to keeps DDL transactional.
  A PostgreSQL-protocol session routed to storage that commits DDL implicitly, such as MySQL, keeps the restriction, because the storage would commit the statement while ShardingSphere metadata is still deferred.

## XA Transaction

### Unsupported

* Recover committing and rolling back in other machines after the service is down.
* MySQL, in the transaction block, the SQL execution is abnormal, and run `Commit`, and data remains consistent.
* After XA transactions are configured, the maximum length of the storage unit name cannot exceed 45 characters.
* PostgreSQL and openGauss have the same metadata-changing DDL support and restrictions described for LOCAL transactions.

## BASE Transaction

### Unsupported

* Does not support isolation level.
* Metadata-changing DDL statements are not supported in transactions for PostgreSQL and openGauss.
  The table DDL that LOCAL and XA transactions support is not supported in BASE transactions.
