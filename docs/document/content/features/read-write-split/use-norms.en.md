+++
pre = "<b>3.3.2. </b>"
title = "Use Norms"
weight = 2
+++

## Supported Items

* Provide the read-write split configuration of one primary database with multiple replica databases, which can be used alone or with sharding table and database;
* Support SQL pass-through in independent use of read-write split;
* If there is write operation in the same thread and database connection, all the following read operations are from the master database to ensure data consistency;
* Forcible primary database route based on SQL Hint;

## Unsupported Items

* Data replication between the master and the slave database;
* Data inconsistency caused by replication delay between databases;
* Double or multiple master databases to provide write operation;
* The data for transaction across primary and replica nodes are inconsistent. 
In the primary-replica replication model, the master nodes need to be used for both reading and writing in the transaction.
