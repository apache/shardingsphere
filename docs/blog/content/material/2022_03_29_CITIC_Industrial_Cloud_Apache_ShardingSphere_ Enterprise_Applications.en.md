+++ 
title = "CITIC Industrial Cloud — Apache ShardingSphere Enterprise Applications"
weight = 46
chapter = true 
+++

_To further understand application scenarios, and enterprises’ needs, and improve dev teams’ understanding of Apache ShardingSphere, our community launched the “Enterprise Visits” series._

On February 28, the Apache ShardingSphere core development team visited CITIC Industrial Cloud Co., Ltd at the invitation of the company. During the meetup, Zhang Liang, Apache ShardingSphere PMC Chair and SphereEx Founder provided details on open source ecosystem building, and ShardingSphere production scenario-oriented solutions.

[CITIC Group](https://www.citic.com/en/#Businesses) is China’s largest conglomerate. It is engaged in a wide variety of businesses, from property development to heavy industry to resources, though its primary focus is the financial sector, including banking and securities.

[CITIC Industrial Cloud Co., Ltd](https://www.citictel-cpc.com/en-eu/product-category/cloud-computing), fully owned by CITIC Group, has developed an open service platform providing industrial internet applications and services in multiple fields such as IoT, smart cities, smart water services — with a desire to promote the digital transformation of traditional industries.

Now, CITIC Industrial Cloud is exploring how to innovate with existing database systems, to support the innovative development of multiple industries.

As one of the most prominent open source middleware projects, with its ever-expanding ecosystem,[Apache ShardingSphere](https://shardingsphere.apache.org/) can meet some of CITIC Industrial Cloud’s technology needs in its development roadmap.

## Ensuring Data Consistency in Data Migration

Enterprises are challenged by ever expanding data related issues. Luckily, Scale-out can solve this problem.

Apache ShardingSphere allows online elastic scaling through its [Scaling tool ](https://shardingsphere.apache.org/document/current/en/features/migration/)and provides multiple built-in data consistency verification algorithms, to ensure the consistency between data source and data on the client-side.

[CRC32](https://crc32.online/) is used by default to strike a balance between speed and consistency; while the verification algorithm supports user-defined SPI.

Since the time data migration takes is proportional to data volumes, to ensure data consistency, a downtime window is required to make the log level. Data will be verified through Scaling, at the business layer, and database layer.

Apache ShardingSphere can divide data migration into multiple sessions and execute them at the same time, merge modification operations of the same record, and execute a line command after configuration to stop write into the primary database, so the database is read-only.

Additionally, SQL execution is suspended during the read-only period since the data is globally consistent when write is disabled. Accordingly, the impact on system usability will be reduced when performing database switches.

## JDBC and Proxy are Designed for Different Core Users

Apache ShardingSphere now supports access through JDBC and Proxy together with Mesh in the cloud (TODO). Users can choose the product that can best suit their needs to perform operations such as data sharding, read/write splitting, and data migration on original clusters.

[ShardingSphere-JDBC](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc) is designed for Java developers. It’s positioned as a lightweight Java framework, or as an enhanced JDBC driver with higher performance. If the performance loss is between 2% and 7%, and you want to optimize the performance, ShardingSphere-JDBC can help reduce the loss to less than 0.1%. Developers can directly access databases through the JDBC client and the service is provided in `.jar`, which means no extra deployment or dependency is needed.

[ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-proxy) is a database management proxy designed for operation and maintenance personnel. Generally, when users need to access databases through Proxy, the network connection may lead to a 30% to 70% performance loss. Therefore, the performance of ShardingSphere-JDBC is much better than that of ShardingSphere-Proxy.

However, the plugin-oriented architecture of ShardingSphere provides dozens of extension points based on SPIs. On the basis of these extension points, ShardingSphere by default implements functions such as data sharding, read/write splitting, data encryption, shadow database stress testing and high availability, and allows developers to extend these functions at will.

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance, lightweight OLTP Java applications.

ShardingSphere-Proxy provides static access and supports all languages, suitable for OLAP applications and sharding database management and operations.

While JDBC can boost development efficiency, Proxy can deliver better O&M performance. Through [DistSQL](https://medium.com/nerd-for-tech/intro-to-distsql-an-open-source-more-powerful-sql-bada4099211), you can leverage Apache ShardingSphere as if you were using a database natively. This helps improve the development capabilities of Ops teams and enterprises’ data management capabilities.

Thanks to the combination of ShardingSphere-JDBC and ShardingSphere-Proxy, while adopting the same sharding strategy in one registry center, ShardingSphere can create an application system suitable for all scenarios. This database gateway-like model allows users to manage all underlying database clusters through Proxy and observe distributed cluster status through SQL, and it can, therefore, enable maintainers and architects to adjust the system architecture to the one that can perfectly meet their business requirements and facilitate business operations.

## ShardingSphere Federated Queries

SQL Federation queries are a query mode providing cross-database querying capabilities. Users can execute queries without storing the data in the same database.

The SQL Federation engine contains processes such as SQL Parser, SQL Binder, SQL Optimizer, Data Fetcher and Operator Calculator, suitable for dealing with co-related queries and subqueries cross multiple database instances. At the underlying layer, it uses [Calcite](https://calcite.apache.org/) to implement RBO (Rule Based Optimizer) and CBO (Cost Based Optimizer) based on relational algebra, and query the results through the optimal execution plan.

As concepts such as data warehouse and data lake are gaining popularity, the applications of ShardingSphere SQL Federation are multiple.

If the user needs to perform a Federation query in a relational database, it can be easily implemented by ShardingSphere. Although data lake deployment is rather complex, ShardingSphere allows federated computations for data inside and outside the data lake by docking with databases, achieving `LEFT OUTER JOIN`,` RIGHT OUTER JOIN`, complex aggregate queries, etc.

## Apache ShardingSphere Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://apacheshardingsphere.slack.com/ssb/redirect)
[
Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

Author
Yacine Si Tayeb

SphereEx Head of International Operations
Apache ShardingSphere Contributor
Passionate about technology and innovation, Yacine moved to Beijing to pursue his Ph.D. in Business Administration and fell in awe of the local startup and tech scene. His career path has so far been shaped by opportunities at the intersection of technology and business. Recently he took on a keen interest in the development of the ShardingSphere database middleware ecosystem and Open-Source community building.
