+++
pre = "<b>3.1. </b>"
title = "Sharding"
weight = 1
chapter = true
+++

## Definition

Data sharding refers to distributing the data stored in a single database to be stored in multiple databases or tables on a certain dimension to improve and extend limits of performance and improve availability. 

An effective way of data fragmentation is to splitting the relational database into libraries and tables. Both database and table splitting can effectively avoid query bottlenecks caused by data flow exceeding the determined threshold. 

Library splitting can also be used to effectively distribute access to a single point of the database. Although table splitting cannot relieve the pressure on the database, it can convert distributed transactions into local transactions as much as possible, which often compounds the problem once cross-library update operations are involved. The use of multi-primary multi-secondary sharding can effectively avoid too much pressure on a single data point, improving the availability of data architecture.

By library and table data sharding, the data volume of each table can be kept below the threshold and traffic can be channeled to cope with high access volume, which is an effective means to cope with high concurrency and massive data. The data sharding method consists of vertical sharding and horizontal sharding.

### Vertical Sharding

According to business sharding method, it is called vertical sharding, or longitudinal sharding, the core concept of which is to specialize databases for different uses. Before sharding, a database consists of many tables corresponding to different businesses. But after sharding, tables are categorized into different databases according to business, and the pressure is also separated into different databases. The diagram below has presented the solution to assign user tables and order tables to different databases by vertical sharding according to business need.

![Vertical Sharding](https://shardingsphere.apache.org/document/current/img/sharding/vertical_sharding.png)

Vertical sharding requires to adjust the architecture and design from time to time. Generally speaking, it is not soon enough to deal with fast changing needs from  Internet business and not able to really solve the single-node problem. it can ease problems brought by the high data amount and concurrency amount,  but cannot solve them completely. After vertical sharding, if the data amount in the table still exceeds the single node threshold, it should be further processed by horizontal sharding.

### Horizontal Sharding

Horizontal sharding is also called transverse sharding. Compared with the categorization method according to business logic of vertical sharding, horizontal sharding categorizes data to multiple databases or tables according to some certain rules through certain fields, with each sharding containing only part of the data. For example, according to primary key sharding, even primary keys are put into the 0 database (or table) and odd primary keys are put into the 1 database (or table), which is illustrated as the following diagram.

![Horizontal Sharding](https://shardingsphere.apache.org/document/current/img/sharding/horizontal_sharding.png)

Theoretically, horizontal sharding has overcome the limitation of data processing volume in single machine and can be extended relatively freely, so it can be taken as a standard solution to database sharding and table sharding.

## Impact on the system

Although data sharding solves problems regarding performance, availability, and backup recovery of single points, the distributed architecture has introduced new problems while gaining benefits.

One of the major challenges is that application development engineers and database administrators become extremely overwhelmed with all these operations after such a scattered way of data sharding. They need to know from which specific sub-table can they fetch the data needed.

Another challenge is that SQL that works correctly in one single-node database does not necessarily work correctly in a sharded database. For example, table splitting results in table name changes, or incorrect handling of operations such as paging, sorting, and aggregate grouping.

Cross-library transactions are also tricky for a distributed database cluster. Reasonable use of table splitting can minimize the use of local transactions while reducing the amount of data in a single table, and appropriate use of different tables in the same database can effectively avoid the trouble caused by distributed transactions. In scenarios where cross-library transactions cannot be avoided, some businesses might still be in the need to maintain transaction consistency. The XA-based distributed transactions are not used by Internet giants on a large scale because their performance cannot meet the needs in scenarios with high concurrency, and most of them use flexible transactions with ultimate consistency instead of strong consistent transactions.

## Related References

- User Guide: [sharding](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/)
- Developer Guide: [sharding](https://shardingsphere.apache.org/document/current/en/dev-manual/sharding/)
