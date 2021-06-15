+++
pre = "<b>3.3.2. </b>"
title = "Use Norms"
weight = 2
+++

## Supported Items

* Provide the replica query configuration of one primary database with multiple replica databases, which can be used alone or with sharding table and database;
* Support SQL pass-through in independent use of replica query;
* If there is write operation in the same thread and database connection, all the following read operations are from the primary database to ensure data consistency;
* Forcible primary database route based on SQL Hint;

## Unsupported Items

* Data replication between the primary and the replica databases;
* Data inconsistency caused by replication delay between databases;
* Double or multiple primary databases to provide write operation;
* The data for transaction across primary and replica nodes are inconsistent. 
In the replica query model, the primary nodes need to be used for both reading and writing in the transaction.
