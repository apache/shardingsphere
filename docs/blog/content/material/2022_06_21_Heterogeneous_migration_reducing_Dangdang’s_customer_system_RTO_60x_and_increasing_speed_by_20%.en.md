+++ 
title = "Heterogeneous migration: reducing Dangdang’s customer system RTO 60x and increasing speed by 20%"
weight = 62
chapter = true 
+++

> [Apache ShardingSphere](https://shardingsphere.apache.org/) helps [Dangdang](https://www.crunchbase.com/organization/dangdang-com) rebuild its customer system with 350 million users, and seamlessly transition from a [PHP](https://www.php.net/)+[SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads) technology stack to a Java+ShardingSphere+[MySQL](https://www.mysql.com/) stack. The performance, availability, and maintainability of its customer system have been significantly improved, which is the best practice of ShardingSphere’s heterogeneous migration.

## Dangdang’s customer system
Dangdang’s customer system is mainly responsible for account registration, login, and privacy data maintenance. Its previous technology stack was based on PHP and SQL Server, which means a standard centralized architecture, as shown in the figure below.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/lorqkp14im9bxbco4yuw.png)
 

Before the rebuild project started, several business modules of the customer system had encountered multiple problems and technical challenges, such as logical decentralization, low throughput, and high operation & maintenance costs.

To improve customers’ shopping experience, Dangdang’s technical team decided to optimize the business logic and underlying data architecture to achieve the availability, scalability, and comprehensive improvement of the customer system in multiple scenarios. The rebuild also introduced many technological innovations such as cross-data source double write, read/write splitting, intelligent gateway, and gray release.

Dangdang’s technical team completed the system rebuild within half a year, from demand design, sharding planning, logic optimization, and stress testing to its official launch.

The project used Java to reconstruct more than ten modules, build distributed database solutions through ShardingSphere & MySQL, and finally complete the online migration of heterogeneous databases. The project boasts the following highlights:

- Reconstruct PHP business code using Java language.
- Replace SQL Server with ShardingSphere & MySQL.
- Complete online data migration of 350 million users.
- Complete a seamless launch through the data double-write scheme.

## Pain points & challenges

**Business pain points**
At the business level, the registration and login logic of some modules of the customer system was scattered at different ends. This resulted in high maintenance costs, and the old technical architecture was limited in terms of performance improvement and high availability.

- **Maintenance difficulty:** the registration and login logic of multiple platforms is scattered, so business maintenance is complicated.
- **Limited performance:** the PHP & SQL Server, a centralized technical architecture, had insufficient throughput.
- **Poor availability and security:** If the active/standby status of SQL Server changes, the subscription database becomes invalid and the reconfiguration takes a window of time. The security of SQL Server running on Windows Server is poor due to viruses, and the upgrading takes a long time (>30min) after the patch is installed.

**Challenges**

- **Data integrity:** the customer system involves data of more than 350 million users. It is necessary to ensure data consistency and integrity after migrating from SQL Server to MySQL.
- **API transparency:** the API is transparent to the caller to ensure that the caller does not change and to minimize the change of interface.
- **Seamless switch:** the business system must be seamlessly switched over without impact on business.
- **Time is short:** the system will be blocked before and after “[618 (aka JD.com Day)](https://edition.cnn.com/2020/06/18/tech/jd-618-china-coronavirus-intl-hnk/index.html) and [11.11 (aka Singles Day)](https://en.wikipedia.org/wiki/Singles%27_Day)” (two online shopping festivals in China), so we need to switch it between the two shopping promotions in a limited window of time, and then undergo the tests to prepare for the 11.11 shopping festival.

## Solutions
**Overall planning**
To improve the maintainability, availability, and performance of the customer system, the R&D team reorganized the customer system architecture.

At the application layer, the goal was to unify the function logic of all terminals and improve business maintainability.

At the database layer, the centralized architecture was transformed into a distributed database architecture to improve performance and availability, which is exactly the open-source distributed solution built by ShardingSphere & MySQL.

- **Application layer:** As Dangdang’s overall technology stack changed, its business development language changed from PHP to Java.
- **Middleware:** As a mature open-source database middleware, ShardingSphere, was used to achieve data sharding.
- **Database:** Multiple MySQL clusters were used to replace SQL Server databases.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/bhrntsoik9fb41raqqbe.png)
 
The overall architecture design introduced multiple schemes, such as distributed primary-key generation strategy, shard management, data migration verification, and gray release.

## Distributed primary-key generation strategy

Distributed primary-key generation strategy is the first problem to be solved if database architecture is to be transformed from a centralized architecture to a distributed one based on middleware.

During the system rebuild, we chose to build two or more database ID-generating servers. Each server had a `Sequence` table that records the current `ID` of each table. The step size of `ID` that increases in the `Sequence` table is the number of servers. The starting values are staggered so that the ID generation is hashed to each server node.

## Implementing sharding (Apache ShardingSphere）
During the customer system rebuild, database sharding was completed through Apache ShardingSphere, and the read/write splitting function was also enabled.

Due to the requirements of the customer system for high concurrency and low latency, the access end chose [ShardingSphere-JDBC](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc), which is positioned as a lightweight Java framework and provides additional services in Java’s JDBC layer.

It connects directly to the database via the client and provides services in the form of a `jar` package without additional deployment and dependence. It can be viewed as an enhanced version of the JDBC driver, fully compatible with JDBC and various [ORM](https://www.techopedia.com/definition/24200/object-relational-mapping--orm) frameworks.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/6yyoi7xsp7qpayu1it5v.png)
 

**Sharding:** ShardingSphere supports a complete set of sharding algorithms, including `modulo` operation, `hash`, `range`, `time`, and customized algorithms. Customers use the `modulo` sharding algorithm to split large tables.
**Read-write splitting:** in addition to Sharding, ShardingSphere’s read/write splitting function is also enabled to make full use of [MHA](https://myheroacademia.fandom.com/wiki/Cluster) cluster resources and improve system throughput capacity.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/chx6mgi5yfr2tqv86ho3.png)
 

## Double-write & data synchronization
Data synchronization runs through the whole rebuild project, and the integrity and consistency of data migration are vital to the rebuild.

This example periodically synchronizes SQL Server’s historical data to MySQL based on Elastic-Job synchronization. During the database switchover, a backup scheme is used to double-write the database to ensure data consistency. The process consists of:

**Step 1:** implement the double-write mechanism

Disconnect link 1, get through links 2, 3, 4, and then 9, 10.

**Step 2:** switch the login service

Disconnect links 9,10, get through link 7 and disconnect link 5.

**Step 3:** switch read service

Get through link 8 and disconnect link 6.

**Step 4:** cancel the double-write mechanism

Disconnect link 2 and complete the switchover.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/c2f4q20g116681261m9m.png)
 

Data verification is performed periodically on both the service side and the database side. Different frequencies are used in different time periods to sample or fully check data integrity. `COUNT/SUM` is also verified on the database side.

Customer system reconstruction adopts an apollo-based gray release. In the process of new login processing, configuration items are gradually released and sequential cutover within a small range is implemented to ensure the launch success rate. The rebuilt system architecture is shown in the following figure.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/iru06ju7apswukev0wxc.png)
 

## Advantages
After the rebuild, the response speed of Dangdang’s customer system is significantly improved, and the daily operation & maintenance costs are also reduced.

The distributed solution provided by ShardingSphere plays a big part in this. The solution is suitable for various high-traffic Internet platform services, as well as e-commerce platforms and other data-processing systems.

- **Performance improvement:** response speed increased by more than 20%.
- **High availability:** RTO is reduced to less than 30s owing to the ShardingSphere & MySQL design.
- **Easy to maintain:** business logic and database maintainability are significantly improved.
- **Seamless migration:** complete online cutover of each module within 6 months, and window time is zero.

## Conclusion
This is ShardingSphere’s second implementation by Dangdang, following the previous one we shared in the post “[Asia’s E-Commerce Giant Dangdang Increases Order Processing Speed by 30% — Saves Over Ten Million in Technology Budget with Apache ShardingSphere](https://shardingsphere.medium.com/asias-e-commerce-giant-dangdang-increases-order-processing-speed-by-30-saves-over-ten-million-113a976e0165)”.

Apache ShardingSphere provides strong support for enterprise systems, as the project strives for simplicity and perfection, to achieve simpler business logic and maximum performance.

**Apache ShardingSphere Project Links:**

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)