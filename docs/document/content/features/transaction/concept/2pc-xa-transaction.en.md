+++
title = "XA Transaction"
weight = 1
+++

2PC transaction submit uses the [DTP Model](http://pubs.opengroup.org/onlinepubs/009680699/toc.pdf) defined by X/OPEN, 
in which created AP (Application Program), TM (Transaction Manager) and RM (Resource Manager) can guarantee a high transaction consistency.
TM and RM use XA protocol for bidirectional streaming. 
Compared with traditional local transactions, XA transactions have a prepared phase, where the database cannot only passively receive commands, but also notify the submitter whether the transaction can be accepted. 
TM can collect all the prepared results of branch transactions before submitting all of them together, which has guaranteed the distributed consistency.

![2PC XA model](https://shardingsphere.apache.org/document/current/img/transaction/2pc-tansaction-modle.png)

Java implements the XA model through defining a JTA interface, in which `ResourceManager` requires an XA driver provided by database manufacturers and `TransactionManager` is provided by transaction manager manufacturers. 
Traditional transaction managers need to be bound with application server, which poises a high use cost. Built-in transaction managers have already been able to provide services through jar packages. 
Integrated with Apache ShardingSphere, it can guarantee the high consistency in cross-database transactions after sharding.

Usually, to use XA transaction, users must use its connection pool provided by transaction manager manufacturers. 
However, when Apache ShardingSphere integrates XA transactions, it has separated the management of XA transaction and its connection pool, so XA will not invade the applications.
