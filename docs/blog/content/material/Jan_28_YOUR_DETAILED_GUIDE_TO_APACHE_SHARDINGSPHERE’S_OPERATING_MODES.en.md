+++
title = "Your Detailed Guide to Apache ShardingSphere’s Operating Modes"
weight = 32
chapter = true
+++

# Your Detailed Guide to Apache ShardingSphere’s Operating Modes

In [Apache ShardingSphere](https://shardingsphere.apache.org/) 5.0.0 GA version, we added the new concept Operating Mode and provided three configuration methods: Memory, Standalone, and Cluster. Why does ShardingSphere provide these operating modes? What are the differences between them in actual development scenarios?

This article is a guide for you to better understand ShardingSphere’s new operating modes.

## Background: Distributed Governance

Distributed governance is the foundation of cluster deployment in ShardingSphere. In previous versions, users needed to configure the governance tag in the configuration file to enable distributed governance:

![1](https://shardingsphere.apache.org/blog/img/Your_Detailed_Guide_to_Apache_ShardingSphere’s_Operating_Modes_img_1.png)

The most important features of distributed governance include persistent user configuration and [metadata](https://dzone.com/articles/shardingshpheres-metadata-loading-process).


They are also the basic capabilities supporting [Distribured SQL](https://opensource.com/article/21/9/distsql) (DistSQL). In the previous ShardingSphere 5.0.0 stories, the core developers of DistSQL have shared the concept of [DistSQL](https://medium.com/nerd-for-tech/intro-to-distsql-an-open-source-more-powerful-sql-bada4099211), its syntax, and its usage in detail, and they have showcased how you can [develop your own DistSQL](https://medium.com/codex/how-to-develop-your-distributed-sql-statement-in-apache-shardingsphere-2939eb689c61).

To recap, DistSQL gives ShardingSphere’s users a database-like experience: users can use DistSQL to build and manage the entire ShardingSphere distributed database ecosystem.

Like other standard SQLs, DistSQL, known as the operating language of the distributed database ecosystem, needs to ensure that any configuration and operations metadata can be persisted to keep data consistency when the system is restored.

In previous versions, only when you enabled distributed governance could the feature be implemented. That’s the reason why DistSQL was only available in the distributed governance scenario in its early development stage.

![2](https://shardingsphere.apache.org/blog/img/Your_Detailed_Guide_to_Apache_ShardingSphere’s_Operating_Modes_img_2.png)

## Why We Created Operating Modes

Based on the cluster deployment capability given by the current distributed governance function, ShardingSphere now redefines its distribution capability as **Cluster Mode**.

The cluster mode supports ShardingSphere as a stateless compute node for multi-instance deployment and with a register center, it can synchronize metadata of all instances in the cluster in real-time.

The mode naturally supports DistSQL: under the mode, you can use DistSQL to perform operations on computing/storage nodes such as node online/offline or disabled.

In the past, DistSQL was restricted to distributed scenarios. To fix the issue, ShardingSphere first needs to figure out how metadata can be stored in a non-distributed environment. The simplest solution is to write metadata to local files and therefore when a service restarts, metadata can be loaded from local files according to different configurations.

Unlike the cluster mode used in the distributed scenario, local files cannot share configurations among multiple ShardingSphere instances in real-time. In **Standalone Mode**, all configuration updates only work in respective instances.

ShardingSphere 5.0.0 not only provides users with better features but also builds stable and user-friendly APIs to optimize user experiences.

In addition to Cluster Mode and Standalone Mode, another useful mode is called **Memory Mode**. Why did we design it? Because some users need to quickly start the integration of ShardingSphere but don’t need persistent configuration. For example, some may use ShardingSphere to quickly verify some functions, or just want to test integration. Given such a scenario requirement, we created Memory Mode.

So far ShardingSphere has three modes, i.e. Memory, Standalone, and Cluster. The operating modes are not difficult to understand in terms of our API design, and they are perfectly suitable in the actual use case scenarios of ShardingSphere. Additionally, the three operating modes can **support DistSQL** to quickly build and manage distributed database services.

The `governance` configuration method is removed from the 5.0.0 version, and instead, we start to use the different operating modes.

![3](https://shardingsphere.apache.org/blog/img/Your_Detailed_Guide_to_Apache_ShardingSphere’s_Operating_Modes_img_3.png)

Next, I’d like to explain the basic concepts of the three operating modes in detail, and show you how to choose the right operating mode when you use ShardingSphere for development.

## Concepts and Application Scenarios

**Memory Mode**

Memory is the default operating mode, so you do not need to configure mode. With this mode, users do not need to configure any persistence components or strategies because any metadata change caused by local initialization configuration or SQL/DistSQL operation, only works in the current thread, and the configurations are restored after the service restarts.

The Memory mode is perfect for integration testing: it’s convenient because developers don’t have to clean running traces after they integrate ShardingSphere and have integration testing.

**Standalone Mode**

ShardingSphere’s Standalone Mode provides local files with a persistence method by default. It can persist metadata information (e.g.data sources and rules) to local files so even when the service restarts, configurations can still be read from the local files to ensure metadata consistency.

The Standalone mode makes it convenient for development engineers to quickly build a local development environment for ShardingSphere, test integration and verify features.

The mode’s configuration is shown as follows:

![4](https://shardingsphere.apache.org/blog/img/Your_Detailed_Guide_to_Apache_ShardingSphere’s_Operating_Modes_img_4.png)

The Standalone mode can persist local files by default. Configurations are persisted in the user directory `.shardingsphere` by default, but you can also customize your storage path by configuring `path`.

**Cluster Mode**

We recommend you apply Cluster Mode in a real deployment and production environment. Moreover, if you adopt hybrid deployment architecture with both JDBC and Proxy, you must use the Cluster mode.

The mode can provide distributed governance capability. By integrating an independently-deployed third-party register center, the mode can realize metadata persistence, share data between multiple instances, and implement state coordination in a distributed scenario. Cluster mode is also the reason why ShardingSphere’s horizontal scaling can greatly enhance computing capabilities and lay the foundation of core features like high availability.

We take Zookeeper as the example, to demonstrate mode configuration:

![5](https://shardingsphere.apache.org/blog/img/Your_Detailed_Guide_to_Apache_ShardingSphere’s_Operating_Modes_img_5.png)

We also compare the differences between the three modes (shown in the table below). Our suggestion is that you consider your needs first and then choose the right mode.

![6](https://shardingsphere.apache.org/blog/img/Your_Detailed_Guide_to_Apache_ShardingSphere’s_Operating_Modes_img_6.png)

## Summary
ShardingSphere’s three operating modes can meet virtually all user needs in various environments from testing, to development, to deployment.

Combined with ShardingSphere’s remarkable pluggable architecture, developers can also flexibly customize the persistence methods of each mode and create their own operating modes to make operating modes more suitable for their development and business needs. If you are interested in distributed governance, feel free to reach out to the ShardingSphere community.

## Apache ShardingSphere Open Source Project Links:
[ShardingSphere Github](https://github.com/apache/shardingsphere)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack Channel](https://apacheshardingsphere.slack.com/join/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/contribute/)

**Author**

Meng Haoran

> SphereEx Senior Development Engineer & Apache ShardingSphere PMC.
> 
> Previously responsible for the database products R&D at JingDong Technology, he is passionate about Open-Source and database ecosystems. Currently, he focuses on the development of the ShardingSphere database ecosystem and open source community building.

![](https://shardingsphere.apache.org/blog/img/Meng_Haoran_Photo.png)







