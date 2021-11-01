+++
title = "Use Norms"
weight = 2
+++

## Supported

* Provide the readwrite-splitting configuration of one primary database with multiple replica databases, which can be used alone or with sharding table and database;
* Primary nodes need to be used for both reading and writing in the transaction;
* Forcible primary database route based on SQL Hint;

## Unsupported

* Data replication between the primary and the replica databases;
* Data inconsistency caused by replication delay between databases;
* Double or multiple primary databases to provide write operation;
* The data for transaction across primary and replica nodes are inconsistent;
In the readwrite-splitting model, primary nodes need to be used for both reading and writing in the transaction.
