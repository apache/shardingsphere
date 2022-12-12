+++ 
title = "A Holistic Pluggable Platform for Data Sharding — ICDE 2022 & Understanding Apache ShardingSphere"
weight = 48
chapter = true 
+++

I bet you have at least once experienced accessibility issues on your preferred online shopping website. Yo may have not even thought much about it, or you may have attirbuted it to a slow internet connction, but the truth is that the increasing concurrent access that databases cannot deal with is one of the reasons.

Then it begs the question: how can we effectively solve the current database challenges? Data sharding could be a solution.

This article will introduce a paper titled “Apache ShardingSphere: A Holistic and Pluggable Platform for Data Sharding” which is co-authored by Chongqing University and SphereEx Lab and published by [International Conference on Data Engineering (ICDE)](https://icde2022.ieeecomputer.my/) 2022, one of the top-level database conferences in the world.
![](https://miro.medium.com/max/1400/1*kiGInZQgDnlF1qjFBP_sGw.png)

## Background
Data expansion and growing needs for data mean that databases are not only supposed to be able to manage large amounts of data, but to also support highly concurrent access.

Therefore, it’s necessary to design a database that can meet these two requirements since it can enhance user experience and solve website accessibility problems.

For now, relational databases are still the best choice for online transaction processing (OLTP). However, a relational database is designed for a single machine, which means its scalability could be poor when faced with large amountsof data, thus failing to resolve the problem of high concurrency cannot be quite resolved.

Although NoSQL databases such as HBase can support high concurrent requests, they are not suitable for OLTP and they may even cause data inconsistency. That’s why NewSQL has drawn a lot of attention, although we should note that “New” means that the database is developed from zero. Though suitable for current application scenarios, this type of database are still relatively untested, and NewSQL users and maintainers still need to fully learn the new technologies.

If you were to ask if it is feasible to insert a data sharding middleware between database and applications to connect and manage multiple existing databases, the answer is a resounding “yes”.

As shown in Figure 1 below, the sharding middleware distributes user queries to multiple databases in different machines, executes the queries separately, merges the multiple results, and returns them to the application, which effectively removes the limitations of a single machine.

For developers, the sharding middleware is transparent and easy to use — therefore making it very developer-friendly and efficient.

![Figure 1 An Example of Data Sharding](https://miro.medium.com/max/1400/1*aQV4IIGsv9_nUZaxtFJVlw.png)

Designing a good data sharding middleware can be rather challenging.

**First**, the underlying databases used in different scenarios vary. These databases use different database protocols and SQL statements, making it difficult to support multiple databases under a unified framework. Additionally, there are many types of SQL statements (from simple queries to aggregations to Multi-Table JOINs, etc.), and route and merge strategies for different SQL statements also are also different.

**Second**, it is difficult to coordinate transactions across multiple databases, and sometimes a single transaction type may not support all scenarios.

**Third**, data sharding middleware could be inefficient since it takes some time to forward requests and merge results.

**Fourth**, database administrators (DBA) have to configure sharding rules one by one, which is time-consuming.

Apache ShardingSphere, as Apache’s first Top-Level open source database sharding project, can tackle all the above-mentioned challenges. The main goal of ShardingSphere is to reduce the impact of data sharding and allow coders to use data sharding databases as if they were using just one database. It provide the following features:

**1.Versatile.** ShardingSphere supports a variety of mainstream relational databases (in fact, it supports any database that meets the SQL-92 standard). A complete SQL engine is designed and implemented in ShardingSphere, so any type of SQL can be routed accurately. Additionally, it provides three types of distributed transactions and a variety of features. As far as I know, ShardingSphere is the first system that includes data sharding, strongly consistent transactions, and flexible transactions.

**2.Efficient.**
 In addition to the proxy mode, ShardingSphere also provides JDBC access, which can greatly improve efficiency in most scenarios. Besides, ShardingSphere creates two execution modes and an intelligent strategy for choosing the appropriate execution mode and result merging strategy, which can help manage resources while ensuring efficiency.

**3.Pluggable and scalable.**
 ShardingSphere is designed on the basis of SPIs (Service Provider Interface, a service discovery mechanism provided by Java JDK) among other design patterns. Therefore, multiple types of databases, functions, and sharding algorithms can be easily added. Furthermore, all existing functions can be removed or combined as needed.
 
**4.User-friendly.** 
ShardingSphere supports almost all SQL statements while hiding the details of distributed transactions. Therefore, with the help of ShardingSphere, application developers can use distributed transactions and sharded database clusters in the same way as standalone databases. ShardingSphere also provides DistSQL and AutoTable strategies to help DBAs handily configure sharding strategies.
Currently, more than 170 companies are using ShardingSphere. This article is based on ShardingSphere 5.0.0 released on November 10, 2021.

## System Architecture and Data Flow
As shown in Figure 2, ShardingSphere can be divided into five modules:

1. **dData source:** It enables storage by integrating various databases and currently supports data sources such as MySQL, PostgreSQL, SQL Server, Oracle, MariaDB and openGuass.
2. **Function:** It provides many out-of-the-box features that can be freely added, combined, or deleted as needed.
3. **Governor** is mainly used for configuration management and health monitoring.
4. **SQL engine.** With the complete data sharding SQL engine, all functions are pluggable, and any function can be implemented through a SQL statement.
5. **Adaptors:** They target different scenarios.

![Figure 2 ShardingSphere Architecture](https://miro.medium.com/max/1400/0*Mmp_mdCL63FmGdrI)

Figure 3 shows data flows of ShardingSphere. There are two types: the first one can enhance and extend JDBC, enabling ShardingSphere-JDBC to run in the same thread with a JAVA application.

In terms of ShardingSphere-Proxy, the process between data source and application support applications written in any programming language. In addition, you can directly connect to ShardingSphere-Proxy through any terminal compatible with MySQL or PostgreSQL protocol (such as MySQL Command-Line client, MySQL Workbench, etc.), which is very friendly to DBAs. The governor, as a separate process, can monitor data sources and maintain the configurations of ShardingSphere-JDBC and ShardingSphere-Proxy.
![Figure 3 ShardingSphere Data Flow](https://miro.medium.com/max/1400/0*O7-t2RBqz5KQuMNT)


Next, let’s take a close look at the feature module, governor, SQL engine and adaptor.

## Features
### Data Sharding
Data sharding is one of ShardingSphere’s most significant features. It can shard data and store it based on certain rules in multiple tables and data sources(data source here refers to the schema, the same below). ShardingSphere involves table sharding and data source sharding, and each sharding supports both vertical and horizontal shardings.

Figure 4 shows the four types of sharding. Figure 4(a) shows the original database with its internal table structures and data. Figure 4(c) shows the vertical table sharding, horizontal table sharding, vertical data source sharding, and horizontal data source sharding. Figure 4(b) shows the table structure after sharding.

Vertical sharding alters the table structure, and therefore the structure has to be frequently adjusted in actual application scenarios and can’t fully meet the fast-changing needs of the internet business. However, horizontal sharding can break the limits of a single machine’s storage capacity and support more flexible extensions.
![](https://miro.medium.com/max/1178/0*YxqRTXBycX4typX4)
![Figure 4 Sharding Instance](https://miro.medium.com/max/1386/0*z97zrQoaXzqirrl7)


Combining Figure 1 and Figure 4, let’s explain some data sharding concepts: the `uid` in Figure 1 is used to determine the database sharding field, known as the sharding key. `uid%2` in Figure 1 is used to distribute data to different tables, which is the sharding algorithm. ShardingSphere has over ten built-in sharding algorithms, and also allows clients to customize algorithms through interfaces.

ShardingSphere provides different types of tables for different data sharding demands. For example in Figure 4, `t_user` is a schema, while `t_user_h0` and `t_user_h1` are physical tables. The actual tables stored in the database are the physical tables, but the tables seen by application developers are un-sharded schemas. Data sharing operation is transparent to application developers.

In Figure 4, if `t_user` and `t_order` share the same sharding algorithm, data source and sharding key, they are binding tables with each other, which is very helpful in related queries. In addition, we use data nodes to map schema to physical tables. It is the smallest unit of sharding, consisting of a data source name and an actual table name, such as `DSO.t_user_h1`.

### Distributed Transaction
ShardingSphere provides three types of distributed transactions for different application scenarios.

**XA Transactions.** Native XA transactions include three roles: Application Program(AP), Resource Manager(RM), and Transaction Manager(TM). Developers will have to pay a high learning cost if they want to use XA transactions. ShardingSphere plays the role of AP and TM, so developers can use XA transactions just like standalone transactions.

As shown in Figure 5, when an application sends a “commit” request to ShardingSphere, ShardingSphere logs it and starts the 2PC (two-phase commit) process — phase one: ShardingSphere will send prepare commands one by one to collect votes from all data sources.

If a data source determines that it can commit its own transaction, it sends an “ok” to ShardingSphere; otherwise, it sends a “no” and rolls back what it has done.

If all the data source feedback is OK, ShardingSphere will send a commit command to all data sources. If there is any No data source feedback, it will send a rollback command to roll back. The data source operates according to the commands.
![Figure 5 XA Transaction](https://miro.medium.com/max/656/0*VNJaxxbqnZbBNmqC)

XA transactions ensure the ACID features (Atomicity, Consistency, Isolation, Durability) of the data, but they are not suitable for long-time transactions as they lock resources. ShardingSphere provides Local Transactions and BASE Transactions to solve this problem.

**Local Transactions.** As in Figure 6, when ShardingSphere receives a `COMMIT` or `ROLLBACK` command from a user application, it transmits the command directly to all data sources. Even if some data sources fail to commit, ShardingSphere will ignore it, because there is no preparation phase for Local Transactions, which can increase efficiency.
![Figure 6 Local Transactions](https://miro.medium.com/max/662/0*zH3FCndUMpfXAjEs)

**BASE Transactions.** A BASE transaction allows data inconsistency for a short period of time. A BASE transaction has to meet three requirements: basically available, soft state and eventually consistent. Generally, BASE transactions can improve systems’ performance, because they do not require strong consistency and can reduce the contention for shared resources.
![Figure 7 BASE Transactions](https://miro.medium.com/max/704/0*qglHQJqz-I1CGcPB)


SharingSphere integrates SEATA, as it provides an automatic transaction mode(AT) to automatically generate compensation operations. As shown in the grey box of Figure 7, SEATA has three roles: 1) The Transaction Coordinator(TC) maintains the state of global and branch transactions and drives global commits or rollbacks; 2)The Transaction Manager(TM) defines the scope of global transactions; 3)Resource Manager(RM) manages resources and drive branch transactions to commit or rollback.

The user application only needs to interact with ShardingSphere with a standard database connection. The BASE transaction in ShardingSphere is a 2PC process where ShardingSphere plays the role of TM and RM in SEATA. the BASE transaction process is shown in Figure 8.
![Figure 8 BASE Transaction Process](https://miro.medium.com/max/1400/0*jx0kLeEidPT5mwqx)

In addition to data sharding and distributed transactions, ShardingSphere also provides many other helpful features, which include but are not limited to: read/write splitting, encryption, shadowing (i.e. creating a shadow database and routing the corresponding test SQL to it), scale-out, etc. All these features are transparent to application developers, as ShardingSphere can intelligently identify the necessary information from standard SQL statements. And we can add, remove or merge other features freely depending on the applications scenarios. More information can be found in our user manual.

## Governor
### Configuration Management
Configuration information is stored in Apache ZooKeeper, a mature and powerful distributed orchestration system that provides efficient memory management and distributed locking services. Developers are now used to operating data via SQL. Therefore, we initiated a new DistSQL (distributed SQL), which allows users to configure ShardingSphere in the same way as they use a database.

DistSQL is divided into Resource & Rule Definition Language (RDL), Resource & Rule Query Language (RQL), and Resource & Rule Administrate Language (RAL):

(1) RDL: To add, modify or delete resources and rules. For example:
![](https://miro.medium.com/max/1400/0*THXK6tjqFfIzU7UV)

We proposed the AutoTable concept. We don’t need to set the sharding rules manually, but just tell ShardingSphere the data source for sharding and how many shards there should be. ShardingSphere will take care of the sharding details.

(2) RQL: To query and display existing resources and rules. For example:
![](https://miro.medium.com/max/1400/0*cHw4_RbNbf3pkXGE)

(3) RAL: Responsible for additional administrator functions, such as switching transaction types and extensions. For example:
![](https://miro.medium.com/max/1400/0*jy5oNDutZWUFT86T)

It can be seen that DistSQL is very similar to SQL and is developer-friendly. More information can be found in our user manual.

### Health Monitoring
To ensure high availability, we can create multiple ShardingSphere-Proxy instances and use a load-balancer to ensure load balancing. The governor will initiate a thread to regularly check the status of each ShardingSphere-Proxy instance and the underlying database, and if changes are found, the governor will automatically change the configuration to ensure system’s proper function.

## SQL Engine
ShardingSphere designs and implements a complete SQL engine for data sharding and other features, which enables users to execute any Apache ShardingSphere function with a single SQL statement.

First, a SQL statement is parsed into an abstract syntax tree by SQL Parser. Then SQL Router matches the logical SQL statement with the data node according to the parsing result.

As mentioned above, what developers view are logical tables, rather than actual tables, so how can coders perform SQL operations in the actual data source? The answer is by utilizing SQL Rewriter that rewrites the SQL statement.

There are two types: one is correctness-oriented rewrites that include the processes such as rewriting the logical identifiers to actual ones (e.g. rewrite `t_user` to `t_user_h0`), deriving columns needed for subsequent merges, modifying pagination from different data sources, splitting `Insert` statements with batches to ensure the correctness of SQL statement execution and avoids data duplication while the other one is referred as optimization-oriented rewrites that only rewrite the identifiers of SQL statements routed to a single node with no other rewriting operations performed to improve processing efficiency.

SQL Executor sends the rewritten SQL statement to the underlying data source and performs related operations. The details are explained below.

Next, multiple results of different data sources obtained from SQL Executor are merged and returned to the client application.

### SQL Executor
This process also focuses on achieving a balance between data source connections, memory consumption, and maximum concurrency besides simply forwarding SQL statements to the underlying data sources.

SQL Executor has two connection modes to automatically balance resource controlling and execution efficiency. In Memory Strictly Mode, memory usage is limited to ensure the number of connections for an operation. For this mode, we prefer to maintain an independent connection for each data node, execute SQL concurrently, load the results through data cursors and merge streams to avoid out of memory or frequent garbage collection. In Connection Strictly mode, connection consumption is strictly limited for each operation by using the memory coalescing technique that loads all result data into memory.

It is difficult for users to choose a suitable connection method and even within the same application, different queries may fit different modes. To make it convenient, we have designed an automatic execution engine which has two phases as shown in Figure 9 below:
![Figure 9 SQL Executor](https://miro.medium.com/max/1400/0*OPZjzMKWsdDkm-qe)


**Phase 1: Preparation**

First, group the route and rewrite results according to data source.

Then based on the condition of data source connections, we can choose a suitable connection mode. If the number of SQL statements routed to the data source is greater than the maximum connections used by the data source for querying, Connection Strictly Mode is chosen, otherwise, Memory Strictly Mode.

Next, we get the required connections and create the execution unit. To avoid deadlocks, we need to add locks to the data source to automatically acquire all the connections needed for the query.

**Phase 2: Execution**

At this stage, the grouped execute units are sent together to the corresponding underlying data source. Data sources then execute SQL concurrently and send `Event` messages for distributed transactions or monitoring.

### Result Merger
Result Merger can merge multiple result sets from different data sources into one and returns the results to the user application.

The above-mentioned merge methods stream merge and memory coalescing are designed for different statements.

Stream is perfect for iterative statements and we only need to merge the results one by one according to the database cursor.

In terms of `ORDER BY` statements, since the result set returned by each data source is ordered, we use stream merge and multiway merge algorithm to combine these result sets into one. If the statement contains both `GROUP BY` and `ORDER BY`, and their attributes are the same, Stream Merge is favorable, because the grouped data records are all located at the first position marked by the cursor. As shown in Figure 10, we scan the pointed data records (marked in orange) from left to right and accumulate scores and after the name is not “Jerry” outputting “Jerry, After 185”, adjusts the database cursor and repeats the above operation. Otherwise, memory coalescing is preferred.
![Figure 10 Example : Stream Merging Used in GROUP BY Statement](https://miro.medium.com/max/1400/0*P5_XxDP02Ej0blcy)


In addition, we can use Stream and Memory Coalescing together for aggregate statements and paging.

## Adapters and Application
There are two types of ShardingSphere adapters, namely ShardingSphere-JDBC and ShardingSphere-Proxy.

The former is an enhanced JDBC driver that integrates the entire SQL engine and other functions provided by ShardingSphere, making it applicable anywhere JDBC is used.

The latter is a proxy server that forwards requests from the application to the data source. It provides a connection pool so different applications and different queries can share the same connection. ShardingSphere-Proxy can disguise itself as a MySQL or PostgreSQL database, making it transparent to application developers. In addition, it supports all programming languages.

## Experiments
**Datasets:**

1) Sysbench: a well-known database benchmarking tool that provides a table that allows users to adjust data volume

2) TPC-C, short for Transaction Processing Performance Council Benchmark C, is a benchmark used to compare the performance of online transaction processing (OLTP) systems. It simulates several transaction types frequently used by stores. The ten tables are organized by warehouse ( about 600,000 entries per warehouse).

Comparative method: MySQL v5.7.26 (MS); PostgreSQL v10.17 (PG); Vitess v12.0.0; Citus v9.0.0; TiDB v5.2.0; CockroachDB v21.1.11 (CRDB); Aurora MySQL v2.07.2; Aurora PostgreSQL v4.2.

We used a cluster with 12 virtual servers in HUAWEI Cloud, each of which was equipped with CentOS 7.1 64-bit, 32-VCORE CPU, 64 GB memory, and 1TB disk.

**Comparative Experiment.** Results from Sysbench runs are compared. As shown in the table below, the ShardingSphere-based system always performed the best in all scenarios.
![](https://miro.medium.com/max/1400/0*_3TF7xTzV7hlgDNB)

In terms of TPC-C, we provided five scenarios, and the proportion of each is fixed. In the article we only state the overall performance. As shown in Figure 11, SSJ works best.
![Figure 11 Comparison with Distributed Systems](https://miro.medium.com/max/1400/0*7JQqs65sKxgoG0sR)


Scalability Testing. Next, we only compare with TiDB, because it has the best performance compared to other systems. As shown in Figure 12, SSJ-based systems always perform best.
![Figure 12 Comparison With Different Data Sizes](https://miro.medium.com/max/1400/0*NlR722dGy8mqoA2l)


In the test with different concurrency numbers, as shown in Figure 13, we find SSJ-based systems perform best in terms of TPS.
![Figure 13 Comparison With Different Concurrencies](https://miro.medium.com/max/1400/0*ySffpKv8q_G2cz86)


When it comes to comparison with different data servers, as shown in Figure 14, TPS increases slightly at first, and then remains stable when the number of data servers exceeds 3. There may be two reasons: first, we only use one proxy server, which may become a bottleneck, and second, with the increasing number of data servers, networking may become another bottleneck.
![Figure 14 Comparison with Different Data Servers](https://miro.medium.com/max/1296/0*qsRLaWRZO7-m_JtY)


We also verified the validity of the binding table. In the figure below, Common shows the performance of table querying without binding. It can be clearly seen that its performance is far less efficient than the binding table.
![Figure 15 Performance of Binding Tables](https://miro.medium.com/max/1400/0*K3UHbnk7E1JtLgT8)


We also tested the effect of the maximum connections MaxCon on efficiency. The result is shown in Figure 16.
![Figure 16 MaxCon Efficiency](https://miro.medium.com/max/1400/0*7di7eGuDWc0M2HTX)

## Conclusion

With Extensive experiments completed by using two well-known benchmarking tools, we verify that under such settings, ShardingSphere outperforms other sharding systems and databases with new architectures in most cases.

As more and more companies are adopting ShardingSphere, we will continue to follow the development guidance concept Database Plus and provide more products to build an plugin-oriented ecosystem with enhanced functions.

## Author

Zheng LI, Chongqing University Spatio-Temporal Lab (CUST)

## Apache ShardingSphere Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)
