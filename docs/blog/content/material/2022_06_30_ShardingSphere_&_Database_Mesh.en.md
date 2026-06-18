+++ 
title = "ShardingSphere & Database Mesh"
weight = 64
chapter = true 
+++

Some time ago, [SphereEx](https://www.sphere-ex.com/en/), a startup formed by [Apache ShardingSphere](https://shardingsphere.apache.org/)’s core contributor team, officially launched the [Database Mesh 2.0](https://faun.pub/database-mesh-2-0-database-governance-in-a-cloud-native-environment-ac24080349eb) concept and its associated open source solution “Pisanix”, sparking discussion and reflection in the community about ShardingSphere and Database Mesh.

Some community users might be wondering why would you go through the trouble of
starting from scratch in a new field when you’re already part of a successful open source project like Apache ShardingSphere? Another question that can come to mind is whether with the cloud-native trend, ShardingSphere will gradually be incorporated into the Database Mesh concept in the future?

With the introduction of the Database Mesh 2.0 concept, SphereEx has charted a different course for open source development, seemingly in conflict with the already successful ShardingSphere, but in reality, the two complement each other while being interdependent.

This post will focus on the ShardingSphere community’s views of the Database Mesh concept, and the future development path and will walk you through the guiding philosophy of Apache ShardingSphere, Database Plus, and how it’s related to the Database Mesh concept.

## What changed from microservices governance to service governance under cloud-native databases?

Compared with microservices, cloud-native database governance has a different focus in terms of feature selection.

First, databases have status and requests that cannot be routed to peer nodes at will as happens with services, making data sharding important for databases. Since database connections are inherently related to the status, starting or stopping a new database instance often means data synchronization and replication, making the ability to auto-discover instances less important compared to microservices.

If the database is viewed as a microservice, although database access can be governed via Service Mesh, it is still subjected to many limitations. At the same time, databases have some special governance attributes, like communication protocols, resource management, load balancing based on data requests, database, and table splitting, observability, access control, etc., all of which cannot be simply understood and solved just by applying the concept of service, and must rely on database reliability engineering.

Hence the development of Database Mesh. SphereEx put forward the Database Mesh 2.0 concept which focuses on how to achieve the following goals in a cloud-native environment:

1. Reduce the burden on developers, improve development efficiency, and provide a transparent and seamless experience when using the database infrastructure.
2. Implement a governance framework covering database traffic, runtime resources, and stability guarantees in a way that is easier to configure, plug, and program.
3. Provide a standard user interface for typical scenarios in multiple database domains such as heterogeneous data sources, cloud-native databases, and distributed databases.
4. Provide capabilities such as data sharding, load balancing, observability, auditing, etc., which have helped solve some traffic governance problems in database governance.

Recently, when business applications started to be packaged and delivered in containers and to be released numerous times every day to Kubernetes infrastructures in various data centers using CI/CD streams, we couldn’t help but think about how to implement database reliability engineering in cloud environments and finally came up with Database Mesh 2.0.

## The difference between ShardingSphere’s Database Plus and Database Mesh

**First, the philosophy is different. In fact, judging from their principles and concepts, Database Plus and Database Mesh are quite different.**

When it comes to Database Mesh, SphereEx believes that cloud-native database governance has some common ground but also possesses its own uniqueness. The common issues can be solved by standardization and automation, while the unique ones can be solved by providing a flexible scaling mechanism that allows engineers to configure and implement on-demand. This requires a high capacity for programming in order to meet scaling requirements when addressing database governance challenges on the cloud.

As for Database Plus, the Apache ShardingSphere community sees it as a design for a distributed database system that aims to build an overall ecosystem including fragmented heterogeneous databases, providing global scalability and overlay computational capabilities on the premise of maximizing the native computational capabilities of the database. It makes the interaction between applications and databases directed by the standards built by Database Plus, shielding the impact database fragmentations might have on the upper layer business.

With the inevitable trend of cloud-native databases, Database Mesh and Database Plus are bound to have some overlap. In a cloud-native and distributed database scenario, there will be more interactions between ShardingSphere and Database Mesh.

**Second, application scenarios are different too.**

ShardingSphere, guided by the Database Plus concept, is primarily used in distributed database scenarios, while Database Mesh is primarily implemented to guide more specific practices for database reliability in cloud-native scenarios.

Being two different designs in two different domains, Database Plus and Database Mesh both represent innovative ideas in their respective data governance scenarios, leading to the creation of the two open-source solutions ShardingSphere and [Pisanix](https://github.com/database-mesh/pisanix).

Database Mesh 2.0 aspires to provide a database-centered governance framework:

1. Databases as a priority: all actions revolve around database governance, such as access control, traffic governance, observability, etc.
2. Engineer-oriented experience: developers, can work on convenient and easy-to-use database declarations and definitions, without caring about the database location. For maintenance & operations teams and DBAs, multiple abstract methods for database governance are provided to automate database reliability engineering.
3. Cloud-native: suitable for different cloud environments with an open ecosystem and implementation mechanism, to build and achieve cloud-native orientation without worrying about vendor lock-in.

On the other hand, with the prospect of achieving connectivity, enhancement, and pluggability, Database Plus aims to build a computing ecosystem on top of fragmented heterogeneous databases to solve the problems of local architecture selection, technology and maintenance & operation high complexity, lack of standards in the databases’ upper layer, and lack of coordination and management among databases.

In a nutshell, Apache ShardingSphere, a project guided by the Database Plus concept, is of great practical value for distributed database scenarios while Database Mesh is the ideal solution for difficulties in database governance in cloud-native scenarios.

**Finally, the business scenarios and demands are different**

From relational databases to distributed databases, multiple database coexistence for enterprise applications has become the norm as application scenarios keep expanding and database performance varies in different scenarios.

Fragmentation is the trend since one single database category cannot be applied to all scenarios. Driven by business scenarios, the use of distributed database solutions has achieved industry consensus, to cope with heavy traffic, higher concurrency, and increasing pressure on the database.

In different scenarios and from different user perspectives, developers are more concerned about operational efficiency, cost overhead, and database protocol types and access information, not so much about where the data is stored. Maintenance & operations teams and DBAs are more concerned about automation, stability, security, monitoring and alerting of database services, etc.

Additionally, DBAs also pay attention to data changes, capacity, secure access, backup, migration, etc. It is the deep understanding of database governance scenarios and the pursuit for a better user experience that together gave birth to the core idea of Database Mesh 2.0: to achieve high performance scaling through programming, and meet the challenges of database governance on the cloud.

## With cloud-native applications booming: will there be a disconnect between ShardingSphere and the cloud?

Back in 2018, when Service Mesh was gaining traction, Apache ShardingSphere PMC Chair Zhang Liang had already taken advantage of Service Mesh and proposed the Database Mesh concept, envisioning whether there was a model that could effectively combine the advantages of JDBC and Proxy clients while avoiding their disadvantages to achieve a true cloud-based infrastructure with auto-scaling + zero intrusion + decentralization. The ShardingSphere Sidecar was the solution born during Database Mesh 1.0 phase.

As distributed approaches and cloud-native went on to become the trend, ShardingSphere, with characteristics of being a community-oriented, open-source project, must keep up with rapid changes happening in different business operations and different scenarios. It has therefore undergone a transformation from Sharding-JDBC to ShardingSphere, changing not only its name but also its positioning and technological ecosystem construction.

**Is Pisanix a cloud-based version of ShardingSphere rewritten in Rust and Go?**

Recently, SphereEx introduced Pisanix, an open source solution for Database Mesh. As for Pisa-Proxy, the core component of Pisanix for database traffic, it shares similarities with ShardingSphere-Proxy making it easy to think of these two as a result of refactoring.

Of course, it is not just Pisanix and ShardingSphere-Proxy, but all MySQL database proxies have a similar architectural design. Especially in regards to databases, there is a certain amount of similarity. The key is to find differences between products. For Pisanix and ShardingSphere-Proxy, they differ in what you want to achieve when you get the data and what problems you want to solve.

Therefore, Pisanix cannot simply be considered a rewrite of ShardingSphere with Rust. They are completely different, except at the entry point because there are always similarities when it comes to databases.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/glyxwapgpvrk6pe1bha6.png)
 

On the other hand, many users will naturally identify Database Mesh and ShardingSphere-Sidecar as the same thing. However, the two are very different at the kernel level.

As an implementation of the Database Mesh concept, Pisanix is now able to take on some of the data governance capabilities of the original ShardingSphere-Sidecar in a cloud-native environment, making it easy for users to use ShardingSphere in a cloud-based environment.

Sidecar does not serve as the kernel of Database Mesh, which must be oriented towards a specific, engineering-related problem, and Sidecar is just one of the forms of deployment Database Mesh concept. It is possible that one-day Pisanix-Proxy will not be structured as Sidecar, or that it evolves into a very thin middle layer. All in all, Sidecar is just a means to achieve governance capabilities.

Database Mesh’s real core focuses on the expansion of user experience and database service governance. If more database types are available in the future and more scenarios, both cloud-based and off the cloud, emerge, the concept of Database Mesh will also be further expanded, which is exactly what Database Mesh is aiming at.

What Database Mesh wants to do is to block out various factors on the cloud, and unify the various behaviors of the upper layer database to govern. However, protocols and maintenance & operation properties of different databases are quite different, so the tricky part lies in whether we can abstract a standard governance behavior.

The technical solution itself is one of many choices, but the essential part is always the user experience. Whether to choose Java or Rust or Python is only one part of the technical solution, with the objective being the vitality of the project and its ecosystem, which must be considered carefully.

## A glance at the future of databases: ShardingSphere + Database Mesh +….

## Can Database Mesh govern ShardingSphere?
If you think of ShardingSphere as a high-performance distributed database, governing ShardingSphere is the same as governing MySQL, TiDB, and other databases for Database Mesh.

So while it can be governed, ShardingSphere itself does not necessarily require support as its design concept, Database Plus, is to enhance those capabilities that MySQL itself does not inherently possess through connectivity, enhancement, and pluggability. Through ShardingSphere, a native database can be combined with the underlying database to deploy more computing power on the application side, turning it into a high-performance distributed database that avoids wasting resources and provides a more cost-effective solution than other distributed databases.

## Opportunities in cloud-native scenarios

Next, let’s take a look at the industry. As we all know, the cloud represents the future, the irreversible direction. Therefore, whether a project, a product, or an idea can serve this cause will directly affect its lifecycle and impact. This is why ShardingSphere is so committed to its cloud-based initiatives.

Guided by the Database Plus concept, Apache ShardingSphere can extend existing features to provide enterprises and cloud computing platforms with more powerful capabilities across different database products. The underlying compatibility with multiple databases and with all kinds of database products in the cloud makes R&D tasks seamless.

Maintaining its neutral position, ShardingSphere can also ensure that the cloud experience is the same as the experience with local operations, supporting multi-cloud architectures and providing the same experience in different cloud infrastructures, which is precisely what ShardingSphere is good at in cloud-native scenarios.

As two completely independent design philosophies, Database Plus and Database Mesh are not in competition with each other. On the contrary, Database Mesh and Database Plus can work well with each other.

Both can provide solutions based on the existing fragmented ecosystem of databases and major issues that users are facing. Despite their different approaches, they both represent the best ways to solve data governance problems in different scenarios.

**Relevant links:**
[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

[Download Link](https://shardingsphere.apache.org/document/current/cn/downloads/)

[Database Mesh](https://www.database-mesh.io/)

[Pisanix Project](https://github.com/database-mesh/pisanix)