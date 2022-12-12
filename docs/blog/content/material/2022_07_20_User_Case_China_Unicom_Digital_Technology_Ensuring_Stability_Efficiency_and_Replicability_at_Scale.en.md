+++ 
title = "User Case: China Unicom Digital Technology - Ensuring Stability, Efficiency, and Replicability at Scale"
weight = 68
chapter = true 
+++  

China Unicom Digital Tech is the subsidiary of [China Unicom](https://www.chinaunicom.com.hk/en/global/home.php), a global leading telecommunication operator with over 310 million subscribers as of 2021.

The company integrates with China Unicom’s capabilities such as cloud computing, Big Data, IoT, AI, blockchain, and security and touches on smart city, industrial internet, ecological protection, cultural tourism, and other fields.

Unicom Digital Tech has accumulated a large number of industry benchmark cases and successfully provided customers with diverse and professional products & services.

## Background

In recent years, dozens of service hotline platforms have been launched with the help of Unicom Digital Tech, in a bid to improve enterprise and government services.

The service hotlines are characterized by high concurrency and large amounts of data. Every time we dial a hotline, a work order record is generated. The business volumes of a hotline during the epidemic have increased several times compared to the past.

In the work order module of the government or emergency services hotlines, to meet the business needs of massive amounts of data and high stability, Unicom Digital Tech adopts [ShardingSphere](https://shardingsphere.apache.org/) to carry out sharding and store work order information.

## Business challenges

Government service hotlines are the main channel through which the government interacts with enterprises and the public. It provides 24/7 services for the public through a single telephone number.

In addition to dealing with work orders, it also involves services such as telephone traffic, Wiki, voice chat, etc.

In the system planning stage, the database layer is the cornerstone of the business system, so the technology selection process is crucial. System stability is a hard indicator, followed by performance, which directly affects the hotline’s service capability.

At the same time, it should allow for easy maintenance and management, and facilitate system upgrades and backend maintenance.

The hotline service raises the following requirements for database architecture selection:

- Maturity and stability
- High performance
- Easy maintenance
- Low coupling of business code

## Why did Unicom Digital Technology choose ShardingSphere?

The technical team conducted several rounds of research and tests in terms of stability, features, access mode, and product performance, and they finally choose the “ShardingSphere + [MySQL](https://www.mysql.com/)” distributed database solution.

- **Advanced concept**

An increasing number of application scenarios have exacerbated database fragmentation. [Database Plus](https://faun.pub/whats-the-database-plus-concepand-what-challenges-can-it-solve-715920ba65aa) is designed to build an ecosystem on top of fragmented heterogeneous databases and provide enhancements capability for the database.

Additionally, it can prevent database binding, achieve independent control, add more features to the original database, and improve the overall feature ceiling of the data infrastructure.

- **Maturity and stability**

The ShardingSphere project started in 2016. Following years of R&D and iterations, the project has been polished and has proven its stability, maturity, and reliability in multiple Internet scenarios.

- **Comprehensive features**

In addition to sharding capability, ShardingSphere is also capable of data encryption & decryption and shadow DB, which were also among the evaluation indicators of the technical team.

The scenario requirements of security and stress testing can be met by a set of technology stacks. ShardingSphere’s capabilities provide improved support for the construction and architecture expansion of the hotline system.

- **High compatibility / easy access**

ShardingSphere is compatible with the MySQL protocol and supports multiple syntaxes. In the development process, there is almost no need to worry about access to SQL as it is really convenient.

- **High performance**

When the hotline is busy, thousands of on-duty operators are online. The work order record includes tens of millions of records, which create a certain demand for performance.

ShardingSphere-JDBC is positioned as a lightweight Java framework with ideal stress test results and data, which can meet the requirements of government system service capabilities.

- **Simplified operation & maintenance**

On the basis of JDBC’s high performance, ShardingSphere also provides a Proxy friendly to ops teams, which can be directly accessed using common clients.

In addition to the trade-offs of the above five key considerations, other database middleware products were also taken into consideration. The following table indicates the comparison between ShardingSphere and [MyCat](http://mycat.sourceforge.net/).
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/mc2d1n3csc1bf9vu26ab.png)
 

## Solutions

As “ShardingSphere+MySQL” ensures stability, ease of use, and ultimate performance, this configuration has been replicated and used in many government and enterprise service hotline projects of Unicom Digital Tech.

- **Deployment mode**
The combination of [ShardingSphere-JDBC](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc) and [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/) ensures both system performance and maintainability.

- **Sharding key / sharding algorithm**
The work order table of the hotline service is relatively large, and the logic is simple to use. Therefore, the work order ID is selected as the sharding key, and the data is distributed through the hash algorithm.

- **Migration**
The new project does not involve historical data, so there is no need to consider the data migration process, and it can be directly used when it goes live.

- **Implementation process**
Owing to the comprehensive evaluation of the technical scheme and multiple copies of the scheme, the implementation process is smooth.

- **System architecture**
The hotline service module adopts the micro-service architecture

The business module of the hotline service adopts the micro-service architecture, with nodes ranging from ten to dozens, depending on the scale of specific provinces and cities. At the database layer, physical machines are used to deploy three-node MHA. Each hotline system requires four sets of MHA in general.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/v8k9lv1l3pjxme0e7sxs.png)
 

## Advantages

> Business

-  the client mode. It ensures the government hotline’s requirements on performance to the greatest degree.
- System stability provides strong support for business continuity.

> R&D

- Excellent compatibility. It is compatible with mainstream database SQL syntax and can be used after docking.
- Comprehensive features. It supports read/write splitting, data encryption, and full-link stress testing, with strong system scalability.

> OPS

- It supports the proxy mode and provides easy maintenance for Ops teams.

> Technology selection

- It prevents database binding and provides enough flexibility for future upgrades.

## Conclusion

The hotline service cases of China Unicom Digital Tech verified ShardingSphere’s capability to support government service scenarios and further proved that ShardingSphere can be used in any industry.

## Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)