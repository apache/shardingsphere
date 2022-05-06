+++
title = "ShardingSphere x JD Baitiao: Story of an Implementation Journey"
weight = 25
chapter = true
+++

>Apache ShardingSphere has become the best solution for JD Baitiao's very large storage scenarios and scale-up. JD's shopping festival weighed very much on our big data storage. I mean, like hundreds of millions of data sets.

>JD Baitiao, an internet credit products provider launched in 2014, has always been challenged by data explosion. Every shopping festival is like a technical test for us, but every time the technical staff professionals take proactive strategic initiatives in data architecture development to ensure everything goes smoothly. 

> --Dongfang Zhang, JD Batiao R&D Director 

## The Baitiao Data Architecture Evolution

Launched in Feb. 2014, JD Baitiao's data architecture has already evolved for several times because of rapid increase in data volume and rapid business growth. 

**From 2014 to 2015**

Baitiao first adopted the Solr + HBase solution to connect core business systems and other systems with key databases. Solr indexing is able to achieve fast search responses and HBase is used to store a massive amount of data. 

* A Solr cluster can help the core database reduce reads and writes stress. 

* However, Solr is not perfect: it has unsatisfying extensibility and intrusions.

**From 2015 to 2016**

The company chose NoSQL to store monthly data tables in Mongo DB clusters, temporarily meeting the demand for importing/exporting massive data in payment settlement scenarios. 

* The change made it quite efficient to query hot data. In addition, thanks to such unstructured data storage, staff can easily modify table structures.

* Nevertheless, the plan was still problematic: bad extensibility, intrusions, and memory hog. 

**From 2016 to 2017**

Growing business and growing data. Tens of billions of data volume imposed more performance and volume pressure on MongoDB. JD Baitiao's Big Data Platform used DBRep and MySQL Slave to capture data changes and store the information data in its message center, to later be written to ES and HBase. 

* This plan was actually much better. It focused on real-time data and improved extensibility. 

* However, Baitiao faced high costs to maintain its code because of the data sharding problem in its architecture.

The architecture evolution of JD Baitiao is just an illustration of rapidly growing Internet consumer finance. All the methods Baitiao adopted in the past were not comprehensive, indicating that every solution would be outdated soon. 

## Time to Adopt a Decoupled Architecture

To ensure good system performance even with rapidly increasing data volumes, the technical team first chose the sharding architecture pattern that not only guaranteed excellent performance, but also kept code under control. 

**The data striping plan was actually based on application architecture:**

The product continuously developed but the early solution had now become one of the biggest problems. The old data sharding plan makes code even more complex and increases maintenance expenditure. Developers have to spend much time adjusting sharding every time when the application upgrades, so they cannot concentrate on their own developments. In fact, tight coupling is to blame for the problem. 

Therefore, the team decided to use a mature sharding component that simplifies system upgrades and architecture changes. To compare Baitiao's shard and ShardingSphere shard, their differences are shown in the following table.

![](https://shardingsphere.apache.org/blog/img/Blog_25_img_1_JD_VS_ShardingSphere_Table.en.png)

For Baitiao, decoupling was the next task. 

Apparently, JD Baitiao‘s data architecture will experience a brand-new journey to decoupling. The following three directions definitely fueled its transformation: 

**Concentration:** instead of database sharding built in its architecture, it applied a sharding component to place more energy on it own product development. 

**Easier Upgrade:** it used a decoupled architecture to simplify the R&D process of system upgrades. 

**Future-Oriented Plan:** it aims to improve system extensibility so that Baitiao will be more capable of confidently holding large online shopping festivals like the 618 Shopping Festival and Double 11 Shopping Festival (also known as Singles Day on November 11th). 

JD Baitiao is a huge business, so its business scenario is truly related to finance, high concurrency, and massive data volumes. Baitiao's sharding component must have the following features:

1. Be a mature product

2. Have excellent performance

3. Be able to handle big data

4. Have an extensible architecture

## The Apache ShardingSphere Solution

The lightweight Java framework ShardingSphere-JDBC is Apache ShardingSpheres's first product providing services such as a Java Database Connectivity (JDBC) API. ShardingSphere JDBC uses jar archive file installation package and allows the client-side to directly connect to databases. So, it requires no extra deployment dependencies. It is like an enhanced JDBC driver fully compatible with JDBC and ORM frameworks.

ShardingSphere - JDBC provides the following features, making it the best solution in the Baitiao scenario. 

**Mature Product:** ShardingSphere - JDBC is a mature product developed for years, and its open source community is very active.

**Excellent Performance:** its micro-kernel and lightweight design hardly hinder performance.

**Minimum Changes:** it supports native MySQL protocol and minimizes R&D workload.

**Extensibility:** users can combine with the migration sync component to easily extend data.

![](https://shardingsphere.apache.org/blog/img/Blog_25_img_2_JD_ShardingSphere_JDBC_en.png)

After being systematically tested for several times, Apache ShardingSphere became JD Baitiao's first choice of data sharding middleware. The cooperation started at the end of 2018. 

## Product Adaptation

To provide Baitiao with better services and support the business, Apache ShardingSphere has made many improvements to its product features and performance during the implementation process. At the same time, the user case helps optimize the product in return. 

**Upgrade the SQL Engine**

Baitiao's business logic is extremely complicated; its diversified scenario demands require the best SQL compatibility levels. Thus, Apache ShardingSphere restructures the SQL parser module to support more SQLs.

* Single data node routing：100% SQL compatibility 

* Multi data node routing：comprehensively support DML, DDL, DCL, TCL and partly DAL. Support features such as pagination, deduplication, ranking, grouping, aggregation and correlated query. 

**Distributed Key**

Apache ShardingSphere provides built-in distributed key generators, such as UUID, and SNOWFLAKE. It also provides its distributed key generation API so that users can develop custom key generation algorithms to meet their special needs. 

**Shard Key Value Injection**

If a SQL has no shard condition, Apache ShardingSphere uses ThreadLoad to manage shard key value, and users can program and add a shard condition to HintManager, making the condition only effective in the current thread. That's so-called SQL zero intrusion.

Additionally, Apache ShardingSphere continues to optimize its other features in order to meet Baitiao's need for high performance such as：

* SQL parsing result cache

* JDBC metadata cache

* Bind table & Broadcast table

* Automated execution engine & Stream merge

JD Baitiao & ShardingSphere teams joint efforts amde all indicators of the product live up to their expectation. The resulting final performance is almost the same as that of a native JDBC. 

![](https://shardingsphere.apache.org/blog/img/Blog_25_img_JD_System_en.png)


## Cutover

Apache ShardingSphere uses its custom HASH strategy to shard data, effectively avoiding the hot data problem. The total of data nodes almost reaches ten thousand. The cutover process lasts for about 4 weeks. 

1. After DBRep reads data, Apache ShardingSphere synchronizes the data to the target database cluster. 

1. Two clusters run together. After a data migration, Baitiao uses its own tool to verify business and data. 

DBRep is fundamental to the product design of ShardingSphere-Scaling. The automation feature of Scaling definitely facilitates migration and scale-up. 

## Apache ShardingSphere Brings Many Benefits

**Simplified Upgrade Path**

The decoupled architecture effectively simplifies a technology stack required in a system upgrade, so the developer team no longer need to worry about table sharding. Instead, they can focus on the business itself. In brief, ShardingSphere greatly helps Baitiao optimize the upgrade path. 

**Save R&D Cost**

Baitiao directly uses the mature product Apache ShardingSphere, so they do not need to reinvent the wheel and save a lot of time and energy. 

**Improve Architecture Extensibility**

Baitiao also uses the sync migration component Scaling that makes system scale-up even more flexible. It's the company's secret for online shopping festival successes. 

## Summary

The growth of JD Baitiao's business greatly stimulates its data architecture upgrades. This time, it chose Apache ShardingSphere to decouple its architecture and make future upgrades less complicated. Thank to this from now on, developers only have to concentrate on their own product optimization because its data architecture is extensible enough. The case is really a good example of ShardingSphere application in the consumer finance scenario. 

Now, there are more and more internet credit payment models. In the future, Apache ShardingSphere in collaboration with JD Technology will explore more business scenarios, and contribute finance-related technological innovations, further improving Internet finance.

### About Apache ShardingSphere

ShardingSphere is one of the Apache Software Foundation Top-Level Open Source projects, used by over 170 enterprises worldwide, across various verticals such as finance, e-commerce, cloud services, tourism, logistics, education, and entertainment. Its GitHub community has so far cumulated over 14,000 stars. 

We welcome more technical professionals to write articles and share their experiences and thoughts. If you are interested, feel free to contact us: 

 ShardingSphere Github: [https://github.com/apache/shardingsphere]() 
 
 ShardingSphere Twitter: [https://twitter.com/ShardingSphere]()
 
 ShardingSphere Slack Channel:[ShardingSphere Slack Channel:]()
