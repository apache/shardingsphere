+++
pre = "<b>3.4.2.1 </b>"
toc = true
title = "Local transaction"
weight = 1
+++

## Function

* Fully support none-cross-database transactions, for example, table sharding only or database sharding with route result in the single database.

* Fully support cross-database transactions caused by logic exceptions, for example, the update of two databases in one transaction. 
After the update, the null cursor is thrown and the content in both databases can be rolled back.

* Do not support the cross-database transactions caused by network or hardware exceptions. 
For example, after the update of two databases in one transaction, one database is down before it is submitted, then only the data of the second database can be submitted.

## Supported Situation

* Sharding-JDBC can support users' own configurations without the use of XA data source.

* Sharding-Proxy do not need support; just use XA or BASE transactions.
