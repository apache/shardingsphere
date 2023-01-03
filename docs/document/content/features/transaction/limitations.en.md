+++
title = "Limitations"
weight = 2
+++

Though Apache ShardingSphere intends to be compatible with all distributed scenario and best performance, under CAP theorem guidance, there is no sliver bullet with distributed transaction solution.

Apache ShardingSphere wants to give the user choice of distributed transaction type and use the most suitable solution in different scenarios.

## LOCAL Transaction

### Unsupported

* Do not support the cross-database transactions caused by network or hardware crash. For example, when update two databases in transaction, if one database crashes before commit, then only the data of the other database can commit.

## XA Transaction

### Unsupported

* Recover committing and rolling back in other machines after the service is down;
* MySQL, in the transaction block, the SQL execution is abnormal, and run `Commit`, and data remains consistent;
* After XA transactions are configured, the maximum length of the storage unit name can not exceed 45 characters.

## BASE Transaction

### Unsupported

* Do not support isolation level.
