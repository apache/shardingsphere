+++ 
title = "Asia’s E-Commerce Giant Dangdang Increases Order Processing Speed by 30% — Saves Over Ten Million in Technology Budget with Apache ShardingSphere"
weight = 41
chapter = true 
+++

> Apache ShardingSphere is an easy-to-use and stable product, making Dangdang’s warehouse management system (WMS) even more powerful. Integrated with WMS, ShardingSphere plays a vital role in reforming the supply chain system.

> - Li Yong, Head of WMS Technology, Dangdang

Ffollowing [Apache ShardingSphere 5.0.0 GA](https://medium.com/codex/apache-shardingsphere-5-0-0-new-features-middleware-to-ecosystem-evolution-e69de00bfb1b) release in November 2021, the [5.1.0 version](https://shardingsphere.medium.com/apache-shardingsphere-5-1-0-now-avaliable-4244ac470e77) was released last month. Having gone through over two years of polishing, ShardingSphere’s plugin-oriented ecosystem is beginning to take shape, and the project embarks on the evolution from a simple data sharding middleware to a mature distributed database ecosystem driven by the concept of [Database Plus](https://www.infoq.com/articles/next-evolution-of-database-sharding-architecture/).

Dangdang, established at the end of 1999, has become a leading e-commerce platform selling books of any kind, and by integrating new Internet technologies with the traditional book industry. Dangdang was founded during the surge in China’s Internet industry in the early 2000s.

Later, the e-commerce industry became exremely competitive in the country, and the market saturated. Facing fierce market competition, e-commerce platforms had to adapt to remain competitive.

In response, Dangdang not only adjusted its business strategies and management approaches, but also upgraded its technology architecture. Dangdang didn’t have its warehouse management and transportation system at that time. However, with growing business volume and technological capabilities, Dangdang needed to rebuild its warehouse management system and transportation management system (TMS) to better satisfy its business needs. For instance, in terms of hardware, it replaced mini-computer with x86, while its old centralized system was transformed to a distributed system with more flexibility.

One of the biggest challenges was massive warehousing data storage. The engineers wanted to adopt the data sharding technology that was often chosen by other big Internet companies. Disappointingly, they failed to find a mature and versatile open source database middleware in the marketplace, and therefore,started to develop a new data sharding product. That’s the origin of Sharding-JDBC. The product was created to bring more possibilities to data services.

Dangdang released its new WMS five years ago, which meant that it completed its intelligent warehousing transformation. Since then, with Apache ShardingSphere, the WMS has enabled Dangdang to hold large online shopping events every year such as Dangdang’s April Reading Festival, Double-Eleven Online Shopping Festival (aka Singles Day), and Mid-Year Shopping Festival, and to manage over a dozen of smart warehouses.

## Business Challenges
When Dangdang adopted a third-party WMS, the database in use was [Oracle](https://www.oracle.com/index.html) based on [IBM](https://www.ibm.com/docs/en/informix-servers/14.10?topic=strategies-what-is-fragmentation) minicomputers.

However, considering the increasing business volume and warehouse order requests, especially during the online shopping festivals, the traditional centralized database architecture of the old WMS became inadequate because the computing and storage capabilities were limited. Additionally, the scale-up solution couldn’t support the system during online shopping festivals, and therefore the developers must do scale-up and adjust the business layer several times to alleviate the storage and computing limits and avoid production risks.

- **Limited computing and storage capabilities**

A centralized architecture is less scalable, making database computing and storage capabilities become the bottleneck.

- **Expensive development and maintenance cost**

Because of poor scalability, developers have to make concessions and scale-up, which increases system development and maintenance costs.

- **Exclusiveness**

If the architecture is not open enough, the system is less flexible, with fewer functions, and difficult to be transformed. The current architecture makes it difficult to quickly adopt new business services such as cloud native ones, SQL audit, data encryption, and distributed governance.

## The Solution
Based on the situation described above, Dangdang’s tech team proposed a warehouse management system solution: in terms of hardware, IBM minicomputer would be replaced with all-purpose x86, and [MySQL](https://www.mysql.com/) would replace Oracle.

However, at that time, there wasn’t a versatile and mature enough open source database middleware living up to Dangdang’s expectations, so they created one and named it Sharding-JDBC.

[ShardingSphere-JDBC](https://shardingsphere.apache.org/document/current/en/concepts/adaptor/#shardingsphere-jdbc) is positioned as a lightweight Java framework that provides additional services at the Java Database Connectivity (JDBC) layer. It is lightweight, efficient, easy to use, and compatible.

With ShardingSphere providing services in the form of the `.jar` files package, users can connect the client directly to the database without additional deployment and dependencies. It can be seen as an enhanced JDBC driver, fully compatible with JDBC and all ORM frameworks.

- Compatible with JDBC and any JDBC-based ORM framework such as JPA, Hibernate, Mybatis, Spring JDBC Template.
- Supports all third-party database connection pools such as DBCP, C3P0, BoneCP, HikariCP, etc.
- Supports all databases implementing JDBC standards. Currently, ShardingSphere-JDBC supports MySQL, PostgreSQL, Oracle, SQL Server, and any database that can be accessed via JDBC.

![The ShardingSphere-JDBC Topography](https://miro.medium.com/max/1400/1*oBhJ6mIXPV4Zo2jjvWVmQQ.jpeg)

Currently, Apache ShardingSphere consists of three products, i.e. JDBC, Proxy, and Sidecar (TODO). ShardingSphere-JDBC and ShardingSphere-Proxy can be deployed independently or together.

It is ShardingSphere-JDBC that is used in Dangdang’s warehouse management system.

> **So how is ShardingSphere-JDBC exactly utilized?**

In the warehouse management system, each warehouse positioned in a physical city is referred to as a unit with its corresponding business system and databases. Each warehouse has three sets of MySQL primary-secondary clusters to load the warehousing data of the designated city. So far, Dangdang has more than ten self-built warehouses all over China, mostly in cities where customers place a large number of orders. This self-built warehouse model is flexible for warehouse management and helps reduce storage costs in the long run.

In terms of architecture, the WMS uses ShardingSphere-JDBC to do database sharding according to their business types, and each cluster stores specified business data. The three MySQL clusters of a single warehouse are divided into three types as follows:

- **Basic:** stores user, area, and menu data.
- **Business:** stores order and package data.
- **Inventory:** stores stock and working data.

Before the release, the system was initialized based on the basic data of a warehouse, such as storage locations.

Next, by deploying the distributed database middleware, Dangdang successfully solved a series of problems such as limited storage and computing capabilities, high costs, and lack of flexibility.
![Topography of Dangdang’s Warehouse Management System (of a Single Warehouse)](https://miro.medium.com/max/1400/1*3P9-I57pExHo8DcMBxs-DQ.jpeg)


## User Advantages
Apache ShardingSphere played a significant role in helping Dangdang develop its WMS. There are five main benefits:

- **Extraordinary performance**

The ShardingSphere-JDBC lightweight framework makes its performance close to a native JDBC. Apart from its great database sharding capability, it helps database performance be taken to the extreme, which allows the WMS to work at full capacity.

- **Keep the system stable**

WMS has been functioning well since its release in 2016.

- **Low risk & zero invasion**

The underlying system of Dangdang has been evolving since 2000. Thanks to its zero-intrusion nature, Apache ShardingSphere can be compatible with others with small modifications to meet Dangdang’s business requirements.

- **Allow developers to focus on the business side** 

The developer team does not need to worry about sharding any more and can concentrate on developing the system to meet business needs.

- **Cost effective and efficient**

Since ShardingSphere is known for its high compatibility, to satisfy increasing business needs, developers don’t have to reconstruct or upgrade the system, minimizing the migration cost.

Warehousing order processing speed is increased by 30% and accordingly, tens of millions of manpower costs are reduced due to the smart warehouses and the auto storage location matching technology.

## Apache ShardingSphere’s Roadmap

Some said that ShardingSphere is a product created by Dangdang. To be precise, ShardingSphere was derived from the company and it also donated ShardingSphere to the Apache Software Foundation (ASF) on November 10, 2018.

After 17-months in the ASF incubator, Apache ShardingSphere successfully graduated on April 15, 2020, as a Top-Level Apache Project.

Recently, to celebrate the third anniversary of ShardingSphere entering Apache Software Foundation(ASF), the community released ShardingSphere 5.0.0. Below is a brief review of Apache ShardingSphere.

- In 2014, Dangdang introduced a centralized development framework targeting at its e-commerce platform called dd-frame. It was created to unify the development framework, standardize its technical components, and achieve efficient cross-team communication by separating business code from technical code. In this way, engineers can devote all their efforts to the business side. The relational database module named dd-rdb in the framework was developed to handle data access and implement the data sharding function. It was the precursor of Sharding-JDBC, as well as a major part of dd-frame 2.x.
- In 2015, Dangdang decided to rebuild its WMS and TMS. As it needed a data sharding plan, the team launched the project in September. In December, 2015, Sharding-JDBC 1.0.0 was released and used within Dangdang.
- In early 2016, Sharding-JDBC was separated from dd-rdb and became open source. The product is an enhanced JDBC driver providing service in .jar files.
- At the end of 2017, Version 2.0.0 was released with the new data governance function.
- In 2018, ShardingSphere was enrolled into Apache Incubator. The release of Version 3.0.0 was a notable turnaround: Sharding-Proxy was released as an independent service. It supported heterogeneous languages, and the project was renamed from Sharding-JDBC to ShardingSphere. It’s in 2018 that the community decided to build the criteria and ecosystem above databases.
- In 2019, Version 4.0.0 was released capable of supporting more database products.
- In 2020, ShardingSphere graduated as a Top-Level Project of the ASF.
- On November 10, 2021, Version 5.0.0 GA was released as a third-anniversity celebration with the whole Apache ShardingSphere community, and the distributed database industry.
![Apache ShardingSphere—Roadmap](https://miro.medium.com/max/1400/0*ejOCiszgebnrZ2kx)

Since Version 5.0.0, Apache ShardingSphere has embarked on its new journey: with the plugin oriented architect at its core, it evloved from a data sharding application to a comprehensive and enhanced data governance tool applicable to various complex application scenarios. Concurrently, Apache ShardingSphere also has more features, and big data solutions.

## Conclusion

Digitization motivated Dangdang to achieve high-quality development and fulfill its mission. ShardingSphere is glad to support Dangdang’s WMS with its cutting-edging data services.

Having gone through two years‘ development, Apache ShardingSphere 5.0.0 GA has been released. The pluggable ecosystem marks an evolution from a data sharding middleware tool to a pioneer in the industry following the “Database Plus” concept.

## Apache ShardingSphere Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)
