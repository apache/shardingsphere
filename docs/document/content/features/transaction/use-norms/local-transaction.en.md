+++
title = "Local Transaction"
weight = 1
+++

## Supported

* Support none-cross-database transactions. For example, sharding table or sharding database with its route result in same database;
* Support cross-database transactions caused by logic exceptions. For example, update two databases in transaction with exception thrown, data can rollback in both databases.

## Unsupported

* Do not support the cross-database transactions caused by network or hardware crash. For example, when update two databases in transaction, if one database crashes before commit, then only the data of the other database can commit.
