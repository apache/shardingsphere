+++ 
title = "Apache ShardingSphere & openGauss: Breaking the Distributed Database Performance Record with 10 Million tpmC"
weight = 51
chapter = true 
+++

Our open source community has cooperated Huawei’s openGauss to build a distributed solution with [Apache ShardingSphere](https://shardingsphere.apache.org/) and [openGauss](https://opengauss.org/en/).

We tested performance together with openGauss on 16 servers for more than one hour. The results were great: our joint solution broke the performance bottleneck of a single machine with a benchmark result of 10 million transactions per minute (tpmC) on average.


## Breaking the 10 Million tpmC Barrier
In this test, the openGauss community ran this TPC-C testing on [BenchmarkSQL](https://www.postgresql.org/message-id/CAGBW59f9q2Y4v-B3D8gje3xUsWz2Z-WaK9wYx4%3DhCY1zN%3D7%2BBQ%40mail.gmail.com) 5.0, which is an open source implementation of the popular TPC/C OLTP database benchmark.

In terms of stand-alone performance, openGauss with ShardingSphere broke the limit of multi-core CPU: two-way 128-Core Huawei Kunpeng reached 1.5 million tpmC, and the memory-optimized table (MOT) engine reached 3.5 million tpmC.

These are great results, but we’re not done. We’ll never stop pushing the boundaries for better database performance — especially in today’s Big Data scenarios and their thirst for top notch database performance.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/tz0oyppd6vh7l1466ug5.png)

In this case, the openGauss team used 7 machines to run BenchmarkSQL adapted to [ShardingSphere-JDBC](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc), connected 8 openGauss databases, and deployed [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc) for data initialization, consistency verification, and other maintenance operations.

Thanks to its database sharding capability, ShardingSphere enabled a total of 8,000 bins of data (over 800 GB) to be distributed across 8 openGauss nodes. Following over 1 hour of test, not only sharding was perfect but the average results also reached over 10 million tpmC, which is the best industry performance at this scale.


## ShardingSphere & openGauss: Building an Ecosystem Cooperation

The Apache ShardingSphere community has been working closely with the openGauss community since 2021.

Faced with the diversification of business scenarios and data volume expansion, the traditional solution that centrally stores data to a single node has since become unable to meet needs in terms of performance, availability, and affordable operation cost.

Database sharding can solve problems of performance, availability, as well as single-point backup and recovery of stand-alone databases — but it also makes distributed architecture more complex.

As the proponent of the Database Plus concept, Apache ShardingSphere aims to build a criterion and ecosystem above heterogeneous databases and enhance the ecosystem with sharding, elastic scaling, encryption features & more. Placed above databases, ShardingSphere focuses on the collaborative way of databases to make reasonable and full use of database compute and storage capabilities.

Currently Apache ShardingSphere has a microkernel plus plugin-oriented architecture model, and on this basis, it continues to improve the capabilities of its kernel and functions to provide increasingly flexible solutions.

Thanks to the design concept of its pluggable architecture, ShardingSphere can support openGauss without additional changes and only needs to increase implementations of the corresponding openGauss database based on the SPI extension points provided by each ShardingSphere module .

Our two communities have collaborated to create a distributed database solution suitable for highly-concurrent Online Transaction Processing (OLTP) scenarios by combining the powerful standalone performance of openGauss with the distributed capabilities provided by the Apache ShardingSphere ecosystem.


## Building an openGauss-based Distributed Database Solution with ShardingSphere
Apache ShardingSphere includes many features such as database sharding, read/write splitting, data encryption, and shadow database. The features can be used independently or in combination.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/jq8uhm2w25yk8r3n8xvh.png)

Currently, ShardingSphere provides users with two access methods, namely ShardingSphere-JDBC and ShardingSphere-Proxy.

ShardingSphere-JDBC can easily and transparently perform operations such as sharding and read/write splitting on databases while meeting high concurrency and low latency needs.

ShardingSphere-Proxy is deployed to add some database capabilities and operations at the proxy level, enabling users to operate ShardingSphere as if it was a native database for a better user experience.

ShardingSphere-JDBC and ShardingSphere-Proxy can be deployed together. We recommend using this mixed deployment in order to make the system user-friendly and perform better.


![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/nla348ta7qgxuwa8ghb6.png)

From the perspective of the openGauss system, Apache ShardingSphere can shard the database horizontally to greatly enhance compute and storage capabilities, as well as database performance.

This means it can effectively solve problems caused by increasing data volume in a single table and can be combined with business data flows to flexibly and smoothly scale out data nodes, intelligently split reads and writes, and implement automatic load balancing of distributed databases.


## Conclusion
Apache ShardingSphere and openGauss can seek potential cooperation opportunities.

Considering the increasingly diversified applicaiton scenarios and increasing data volume, the requirements for database performance are at an all time high and will only continue to increase in the future.

The success of our two communities cooperation is just the beginning of ourtwo communities building a collaborative database ecosystem.

💡 About openGauss

openGauss is an open source relational database management system. It has enterprise-grade features such as multi-core high performance, full-link security, and intelligent operation.

It integrates Huawei’s years of kernel development experience in the database field and makes adaptations and optimizations on architecture, transaction, storage engine, optimizer, and ARM architecture.

💡 About TPC-C

Transaction Processing Performance Council Benchmark C or TPC-C is a benchmark used to compare the performance of online transaction processing (OLTP) systems. It was released by Transaction Processing Performance Council (TPC) in 1992. The latest update is TPC-C v5.11 published in 2010.

TPC-C involves a mix of five concurrent transactions of different types and complexity either executed online or queued for deferred execution. The database is comprised of nine types of tables with a wide range of record and population sizes.

TPC-C is measured in transactions per minute (tpmC). While the benchmark portrays the activity of a wholesale supplier, TPC-C is not limited to the activity of any particular business segment, but, rather represents any industry that must manage, sell, or distribute a product or service.


Apache ShardingSphere Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)
