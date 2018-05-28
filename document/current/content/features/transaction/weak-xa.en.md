+++
pre = "<b>3.4.2. </b>"
toc = true
title = "Weak XA"
weight = 2
+++

## Concept

* Support the none-cross-database transactions, e.g. table sharding without database sharding, or database sharding with the queries routed in the same database.

* Support the exception handling for the cross-database transactions due to logical exceptions. For example, in the same transaction, you want to update two databases, Sharding-JDBC will rollback all transactions for all two database when a null pointer is thrown after updating.

* Do not support the exception handling for the cross-database transactions due to network or hardware exceptions. For example, in the same transaction, you want to update two databases, Sharding-JDBC will only commit the transaction for second database when the first database is dead after updating.
