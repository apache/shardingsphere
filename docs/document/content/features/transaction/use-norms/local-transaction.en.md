+++
title = "Local Transaction"
weight = 1
+++

## Supported Items

* Fully support none-cross-database transactions, for example, sharding table or sharding database with its route result in one database;
* Fully support cross-database transactions caused by logic exceptions, for example, the update of two databases in one transaction, after which, databases will throw null cursor and the content in both databases can be rolled back.

## Unsupported Items

* Do not support the cross-database transactions caused by network or hardware exceptions. For example, after the update of two databases in one transaction, if one database is down before submitted, then only the data of the other database can be submitted.
