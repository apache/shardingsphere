+++
title = "A Distributed Database Middleware Ecosystem Driven by Open Source"
weight = 18
chapter = true
+++

On July 21, 2021, Pan Juan, the SphereEx Co-Founder and Apache ShardingSphere PMC, was invited to give a keynote session at the 2021 AWS Cloud Summit Shanghai, on “Apache ShardingSphere: Open-Source Distributed Database Middleware Ecosystem Building”.

She introduced the expansion of the Open-Source project, community building, and how ShardingSphere practices the “Apache Way”. This article is a summary of Pan Juan’ s ideas.

## A New Ecosystem Layer Positioned Above the Database & Under Business Applications


Different industries, different users, different positionings, different requirements. Today’s databases are faced with more complex data application scenarios, and increasingly personalized and customized data processing requirements than in the past. Demanding environments are driving different databases to continuously maximize data read and write speed, latency, throughput, and other performance indicators.

Gradually, data application scenarios with a clear division of labor lead to the fragmentation of the database market, and it is difficult to produce a database that can perfectly adapt to all scenarios. Therefore, it’s very common for enterprises to choose different databases in different business scenarios.

Different databases bring about different challenges. From a macro perspective, there are some commonalities among these challenges, and it’s possible to base on the commonalities and form a set of factual standards. When you can build a platform layer that can uniformly apply and manage data on top of these databases, even if underlying database differences still exist, you can develop a systemin accordance with certain fixed standards. This standardized solution will greatly reduce the pressure and the learning cost for users managing basic data facilities.

Apache ShardingSphere is the platform layer. Thanks to its repurposing of an original database, it can help a technical team develop incremental capabilities such as fragmentation, encryption, and decryption, etc. It does not need to consider the configuration of an underlying database and can shield users’ perception. Therefore, it can quickly connect business-oriented databases in a direct way and easily manage large-scale data clusters.  

## How to Practice the Apache Way

When a business grows bigger, one database can no longer support a large volume of business data and thus it’s necessary to expand the database horizontally. That is the problem of distributed management. ShardingSphere builds a hot-plugging function layer above a database, while providing traditional database operations, shields users’ perception of the underlying database changes, and enables developers to manage large-scale database clusters by using a single database. ShardingSphere mainly includes the following four application scenarios:

* **Sharding Strategy**

When the volume of a business increases, the pressure of data fragmentation will increase, and thus its fragmentation strategy will become increasingly complex. ShardingSphere enables users to unlock more fragmentation strategies apart from horizontal scaling in a flexible and scalable way at the minimum cost. It also supports custom scaling.

* **Read and Write Splitting**

Usually, master-slave deployment can effectively relieve database pressure, but if there is a problem in a machine or a table of a certain cluster, it’s impossible to have read and write operations, and this problem will have a great impact on the business. To avoid this, developers usually need to rewrite a set of highly available strategies to change the position of master/slave between read and write tables. ShardingSphere can automatically explore all cluster states, so it can immediately find problems such as unreliable requests, and master-slave switching of a database. It also can automatically restore the old master/slave state, which cannot be perceived by the user.

* **Sharding Scaling**

As a business grows, it’s necessary to split data clusters again. ShardingSphere’s The scaling component enables a user to start a task with only one SQL command and shows the running status in real time in the background. Thanks to the “pipeline-like” scaling, the old database ecosystems are connected to a new database ecosystem . 

* **Data Encryption and Decryption**

In terms of database applications, encryption and decryption of key data is very important. If a system fails to monitor data in a standardized way, some sensitive data may be stored as plaintext, and users would need to encrypt them later. It’s a common problem for many teams.

ShardingSphere standardizes the capability and integrates it into its middleware ecosystem, and therefore it can automate new/old data desensitization and encryption/decryption for users. The whole process can be achieved automatically. At the same time, it has a variety of built-in data encryption and decryption/desensitization algorithms, and users can customize and expand their own data algorithms if necessary.

## A Pluggable Database Plus Platform

Faced with various requirements and usage scenarios, ShardingSphere provides developers of different fields with three accesses: JDBC for Java, Proxy for heterogeneous databases and Sidecar for Cloud. Users can make a choice based on what they need, and operate on fragmentation, read and write separation, and data migration of original clusters.

* **JDBC Access:** an enhanced JDBC driver that allows users to fully use JDBC mode, because it is compatible with JDBC and various ORM frameworks. Thus, without additional deployment and dependence required, users can realize distributed management, horizontal scaling, desensitization and so forth.

* **Proxy Access:** a simulation database service that uses Proxy to manage underlying database clusters, which means that users do not need to change their existing mode .

* **Cloud-based Mesh Access:** a deployment form that ShardingSphere designs for public cloud. Recently, SphereEx has joined the startup program of Amazon Web Services (aws), and will cooperate with aws in its China marketplace and beyond and provide aws users with more powerful image proxy deployment. aws and SphereEx will jointly create a more mature cloud environment for enterprise applications.

## Open-Source Makes Personal Work Connected to the World

ShardingSphere is quite influential in its industry. Now, when users need to find a horizontal scaling tool in China, ShardingSphere is usually on their candidate list. Of course, ShardingSphere’s development is not only due to the project maintenance team making valuable contributions over the years, but also to the increasingly active Open-Source community in China.

In the past, most of Chinese Open-Source communities’ users just downloaded programs and looked for code references, but they rarely involved in community building. In recent years, the Open-Source concept is becoming increasingly popular in China, and thus more and more people with strong technical skills have joined the community. It is with their participation that the ShardingSphere community has become increasingly active. But how to evaluate a good Open-Source project? The criteria are not limited to its concept and technology, but also to its deep foundation accumulated in its technical influence, its Open-Source influence, its ecosystem expansion, and its developer group.

For this reason, ShardingSphere, one of Apache’s Top-Level projects, still actively calls on more people to join Open-Source communities. These communities are an excellent way to broaden one’s horizons, be more open-minded and cooperative, and rediscover self-value.

**Project Links:**

ShardingSphere Github: [https://github.com/apache/shardingsphere]()

ShardingSphere Twitter: [https://twitter.com/ShardingSphere
]()

ShardingSphere Slack Channel: [https://bit.ly/3qB2GGc]()

