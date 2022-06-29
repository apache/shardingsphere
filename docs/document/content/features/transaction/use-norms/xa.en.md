+++
title = "XA"
weight = 2
+++

## Supported

* Support Savepoint;
* PostgreSQL/OpenGauss, in the transaction block, the SQL execution is abnormal，then run `Commit`，transactions are automatically rollback;
* Support cross-database transactions after sharding;
* Operation atomicity and high data consistency in 2PC transactions;
* When service is down and restarted, commit and rollback transactions can be recovered automatically;
* Support use XA and non-XA connection pool together.

## Unsupported

* Recover committing and rolling back in other machines after the service is down;
* MySQL,in the transaction block, the SQL execution is abnormal, and run `Commit`, and data remains consistent.
