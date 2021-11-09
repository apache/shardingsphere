+++
title = "Transaction"
weight = 2
chapter = true
+++

Using distributed transaction through Apache ShardingSphere is no different from local transaction.
In addition to transparent use of distributed transaction, Apache ShardingSphere can switch distributed transaction types every time the database accesses.

Supported transaction types include local, XA and BASE. 
It can be set before creating a database connection, and default value can be set when Apache ShardingSphere startup.
