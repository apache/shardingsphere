+++
title = "openGauss X ShardingSphere: One of the Top Distribution Solutions"
weight = 23
chapter = true
+++

## About openGauss

openGauss is an open source relational database management system. Since its launch in June 2020, it has made waves the database management vertical. With an increasing number of partners and developers, it’s already reaping the benefits of the significant efforts & resources poured into building a successful database ecosystem.

## Project Background

Facing problems like data volume explosion, and super-high levels of concurrent data processing, openGauss made a choice: to adopt a distributed solution. It started to work on big data storage, throughput improvement for super-high concurrency, improving performance bottlenecks caused by large data amounts in a table, etc.

Apache ShardingSphere facilitates the distributed feature development of openGauss. Such collaboration on the full stack and open source solution helps openGauss make significant breakthroughs.

## The Distributed Solution

openGauss integrates many open source components to optimize its full stack & open source distributed solution for horizontal scaling, distributed transactions, and distributed governance. Its framework is shown in Fig.1

![](https://shardingsphere.apache.org/blog/img/Blog_23_img_1_The_Structure_of_the_ShardingSphere_openGauss_Distribution_Solution.en.png)

ShardingSphere-Proxy is the open source distributed database solution. ShardingSphere’s well-known features include but are not limited to data sharding, distributed transaction, elastic scaling, and read-write splitting.

HAProxy uses Patroni REST API to identify database master nodes and guarantee high availability, as well as load balancer.

Every high availability node of Patroni supports one master with multiple slaves and uses the Praxos protocols to keep data consistency, so that nodes are allowed to be deployed in the same site or in different sites to ensure multiple sites and centers data security.

In terms of the solution, ShardingSphere-Proxy contributes with its mighty distribution features, Kubernetes helps manage clusters, and prometheus monitors cluster status. This all-star team’s integration make the project a really powerful full stack, open source distribution solution.

## What’s Special About the Project?

* *Great extensibility and elastic scaling*

Horizontal scaling helps realize the linear extension of computing and storage capabilities. The maximum shard count increases to 6400. Database performance improves accordingly and the problem of data volume explosion in single database is effectively managed.

Based on users’ traffic condition, a user can flexibly scale data nodes, and adopt intelligent read-write splitting to realize automatic load balancing of a distributed database.

* *Enhanced enterprise-grade features*

The project provides users with various enterprise-grade features such as distributed data store, Trigger, distributed governance, comprehensive end-to-end encryption, and Workload Diagnosis Report (WDR).

* *Simple deployment*

It may take only a few seconds to deploy the project. Its deployment is simple while also being efficient as it adopts standardized image deployment to keep delivery consistent in different environments. It also uses container deployment as well as resource pooling so that it is less platform dependent.

* *Super-high availability and remote disaster recovery*

The project can effectively manage clusters and facilitate operations & maintenance. It follows the Paxos protocols to ensure data security and strong consistency and provides various features for disaster recovery, whose recovery point object (RPO) is zero. It supports resilient recovery methods for one region, cross-region, and multi-region & multi-center.

* *Open source ecosystem*

The open source project openGauss warmly welcomes more contributors, and encourages joint efforts to grow the open source database ecosystem.

## Application Scenarios

* *Financial system*

The product is extremely reliable even for demanding financial systems. Paxos is a family of protocols for solving consensus in a network of unreliable and fallible processors. Besides being Paxos-based, the product includes powerful features such as distributed transaction with strong consistency, and multi-region multi-center disaster recovery whose RPO is 0.

* *Government and enterprise operations*

It’s really safe and convenient for government and enterprise operations due to its comprehensive end-to-end security protection, its hardware and software ecosystem support, and its high availability capabilities.

* *Intelligent grid*

The product’s flexible disaster recovery features can improve smart grid database scalability.

### ShardingSphere Community:

ShardingSphere Github: [https://github.com/apache/shardingsphere]()

ShardingSphere Twitter: [https://twitter.com/ShardingSphere]()

ShardingSphere Slack Channel: [apacheshardingsphere.slack.com]()



