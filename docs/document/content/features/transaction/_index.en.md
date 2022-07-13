+++
pre = "<b>4.5. </b>"
title = "Distributed Transaction"
weight = 5
chapter = true
+++

## Definition

Four properties of transactions: ACID （Atomicity、Consistency、Isolation、Durability).

- Atomicity: transactions are executed as a whole, and either all or none is executed.
- Consistency: transactions should ensure that the state of data remains consistent after the transition.
- Isolation: when multiple transactions execute concurrently, the execution of one transaction should not affect the execution of others.
- Durability: when a transaction committed modifies data, the operation will be saved persistently.

Distributed transactions guarantee the ACID properties in distributed scenarios, where a single transaction involves operations on multiple data nodes.

## Related Concepts

### XA Protocol

The original distributed transaction model of XA protocol is the "X/Open Distributed Transaction Processing (DTP)" model, XA protocol for short, which was proposed by the X/Open international consortium.

## Limitations

Though Apache ShardingSphere intends to be compatible with all distributed scenario and best performance, under CAP theorem guidance, there is no sliver bullet with distributed transaction solution.

Apache ShardingSphere wants to give the user choice of distributed transaction type and use the most suitable solution in different scenarios.

### LOCAL Transaction

#### Supported

* Support none-cross-database transactions. For example, sharding table or sharding database with its route result in same database;
* Support cross-database transactions caused by logic exceptions. For example, update two databases in transaction with exception thrown, data can rollback in both databases.

#### Unsupported

* Do not support the cross-database transactions caused by network or hardware crash. For example, when update two databases in transaction, if one database crashes before commit, then only the data of the other database can commit.

### XA Transaction

#### Supported

* Support Savepoint;
* PostgreSQL/OpenGauss, in the transaction block, the SQL execution is abnormal，then run `Commit`，transactions are automatically rollback;
* Support cross-database transactions after sharding;
* Operation atomicity and high data consistency in 2PC transactions;
* When service is down and restarted, commit and rollback transactions can be recovered automatically;
* Support use XA and non-XA connection pool together.

#### Unsupported

* Recover committing and rolling back in other machines after the service is down;
* MySQL,in the transaction block, the SQL execution is abnormal, and run `Commit`, and data remains consistent.

### BASE Transaction

#### Supported

* Support cross-database transactions after sharding;
* Support RC isolation level;
* Rollback transaction according to undo log;
* Support recovery committing transaction automatically after the service is down.

#### Unsupported

* Do not support other isolation level except RC.

#### To Be Optimized

* SQL parsed twice by Apache ShardingSphere and SEATA.


## How it works

ShardingSphere provides begin/ commit/rollback traditional transaction interfaces externally, and provides distributed transaction capabilities through LOCAL, XA and BASE modes.

### LOCAL Transaction

LOCAL mode is implemented based on ShardingSphere's proxy database interfaces, that is begin/commit/rolllback.
For a logical SQL, ShardingSphere starts transactions on each proxied database with the begin directive, executes the actual SQL, and performs commit/rollback.
Since each data node manages its own transactions, there is no coordination and communication between them, and they do not know whether other data node transactions succeed or not.
There is no loss in performance, but strong consistency and final consistency cannot be guaranteed.

### XA Transanction

XA transaction adopts the concepts including AP(application program), YM(transaction manager) and RM(resource manager) to ensure the strong consistency of distributed transactions. Those concepts are abstracted from [DTP mode](http://pubs.opengroup.org/onlinepubs/009680699/toc.pdf) which is defined by X/OPEN group.
Among them, TM and RM use XA protocol to carry out both-way communication, which is realized through two-phase commit.
Compared to traditional local transactions, XA transaction adds a preparation stage where the database can also inform the caller whether the transaction can be committed, in addition to passively accepting commit instructions.
TM can collect the results of all branch transactions and make atomic commit at the end to ensure the strong consistency of transactions.

![Two-phase commit model](https://shardingsphere.apache.org/document/current/img/transaction/overview.png)

XA transaction is implemented based on the interface of ShardingSphere's proxy database xa start/end/prepare/commit/rollback/recover.

For a logical SQL, ShardingSphere starts transactions in each proxied database with the xa begin directive, integrates TM internally for coordinating branch transactions, and performs xa commit /rollback.
Distributed transactions based on XA protocol are more suitable for short transactions with fixed execution time because the required resources need to be locked during execution.
For long transactions, data exclusivity during the entire transaction will have an impact on performance in concurrent scenarios.

### BASE Transaction

If a transaction that implements ACID is called a rigid transaction, then a transaction based on a BASE transaction element is called a flexible transaction.
BASE stands for basic availability, soft state, and eventual consistency.

- Basically Available: ensure that distributed transaction parties are not necessarily online at the same time.
- Soft state: system status updates are allowed to have a certain delay, and the delay may not be recognized by customers.
- Eventually consistent: guarantee the eventual consistency of the system by means of messaging.

ACID transaction puts a high demand for isolation, where all resources must be locked during the execution of transactions.
Flexible transaction is to move mutex operations from the resource level to the business level through business logic.
Reduce the requirement for strong consistency in exchange for higher system throughput.

ACID-based strong consistency transactions and BASE-based final consistency transactions are not a jack of all trades and can fully leverage their advantages in the most appropriate scenarios.
Apache ShardingSphere integrates the operational scheme taking SEATA as the flexible transaction.
The following table can be used for comparison to help developers choose the suitable technology.

|          | *LOCAL*       | *XA*              | *BASE*     |
| -------- | ------------- | ---------------- | ------------ |
| Business transformation  | None            | None               | Seata server needed|
| Consistency    | Not supported         | Not supported             | Final consistency       |
| Isolation    | Not supported        | Supported           | Business side guaranteed  |
| Concurrent performance | no loss        | severe loss          | slight loss       |
| Applied scenarios  | Inconsistent processing by the business side | short transaction & low-level concurrency | long transaction & high concurrency |

## Related references
- [YAML distributed transaction configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction/)
