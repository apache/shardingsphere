+++ 
title = "Apache ShardingSphere Enterprise Applications — Bilibili"
weight = 60
chapter = true 
+++

> To further understand application scenarios, and enterprises’ needs, and improve dev teams’ understanding of Apache ShardingSphere, our community launched the “Enterprise Visits”
> series.


[Apache ShardingSphere](https://shardingsphere.apache.org/)’s core contributor team was invited to the headquarters of [Bilibili](https://www.bilibili.com/) in Shanghai. Our PMC Chair, Zhang Liang, discussed with Bilibili’s tech team the e-commerce and digital entertainment verticals application scenarios, and the capabilities of different versions of ShardingSphere.

With the unprecedented growth of data volume and increasingly diversified data application scenarios, different platforms, businesses, and scenarios have resulted in database applications’ fragmentation. The challenges facing databases today are totally different from the ones they were conceived to solve. This is also reflected in the growth of Bilibili’s e-commerce business.

For over a decade, Bilibili has built an ecosystem community centered on users, creators, and content, while producing high-quality videos as well. With an increasing number of users, Bilibili has also gradually developed peripheral business ecosystems such as its subscription revenue model. The expansion of business lines and application scenarios brought great challenges for Bilibili’s tech team, especially in the management and application of backend data.

During the visit, our community and Bilibili mainly discussed these three points:

## The SQL warm-up feature jointly built with Bilibili:
When Bilibili queries goods and orders with batch priority in its business, it is often necessary to manually warm-up SQL during the initial linking process to improve overall performance. However, in the process of manual warm-up, parameters of different lengths cannot be used as a SQL to warm-up due to the use of `foreach` syntax of the [Mybatis](https://mybatis.org/mybatis-3/index.html) framework.

Apache ShardingSphere can see the SQL execution plan through the `preview`, thus warming up the SQL. In the future, we plan to provide a separate SQL warm-up interface and merge the SQL warm-up time into the startup time. ShardingSphere will start on its own after SQL is warmed up.

[Apache ShardingSphere](https://shardingsphere.apache.org/) is an open-source project driven by the needs of the community. Currently, many capabilities’ development is driven by specific user requirements, which are fed back to the community after development and gradually integrated into Apache ShardingSphere.

Therefore, the ShardingSphere community invited Bilibili’s tech team to get involved in terms of the SQL warm-up feature.

With the expansion of Apache ShardingSphere’s application scenarios, there is a higher expectation for ShardingSphere’s capabilities to adapt to a variety of special business scenarios. In the previous “Enterprise Visits” series, the ShardingSphere community recognized that even though it has more than 100 feature modules, enterprises from various verticals have different expectations from ShardingSphere.

## Bilibili’s circuit breaker automation in traffic spike scenarios:

With the growth of Bilibili’s e-commerce scale, as is often the case with many e-commerce businesses, started to adopt front-end strategies such as cash rebates and flash deals in order to improve user retention.

On the backend, however, when the rebate performance of the warehouse is equal to the number of concurrent transactions while exceeding its own connection number limit, it can only be stopped through manual disconnection via DBA based on the number of SKU at the business level. Manual intervention is inefficient and consumes DBAs’ energy, so [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/) is expected to provide an automatic circuit breaker capability.

However, the flash deal scenario is too demanding, so [Redis](https://redis.io/) still represents a better choice. Achieving automation simply requires setting rules and thresholds.

Sinking data to Proxy can also achieve the capabilities of Redis, but no matter how the upper layer changes, the bottom of the database will not change. Therefore, it is better for DBAs to take the initiative to operate the circuit breaker mechanism. Otherwise, after setting a threshold, if the connection between the application and the database has a slight timeout, a large number of transactions will be cut off instantly, which can easily cause a business outage.

Currently, a better way is to dig out all kinds of key information and display it on the visual interface based on Proxy, to facilitate the real-time comparison and operation of DBA instead of achieving automation.

## The Apache ShardingSphere registry:
In the Apache ShardingSphere architecture, the registry provides distributed governance capabilities and is fully open to users since its computing node ([Shardingsphere-proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/)) is stateless without data storage capability. Therefore, user accounts and authorization information need to be stored in the registry.

Concurrently, with the help of the registry, Apache ShardingSphere can distribute information to multiple computing nodes in the cluster in real-time, greatly reducing maintenance costs for users when using the cluster and improving management efficiency.

In cluster mode, Apache ShardingSphere integrates third-party registry components [ZooKeeper](https://zookeeper.apache.org/) and [Etcd](https://etcd.io/) to achieve metadata and configuration sharing in the cluster environment. At the same time, with the help of the notification and coordination ability of the registry, it ensures the real-time synchronization of the cluster when the shared data changes. And the business will not be aware of changes from the registry.

## Q&A with Bilibili
**Q: Is there a performance loss when using JDBC to connect to the governance center?**

A: No. It only connects to the governance center during initialization, and the governance center will send a push when there is a change.

**Q: How does [Sysbench](https://wiki.gentoo.org/wiki/Sysbench) conduct a stress test on Apache ShardingSphere?**

A: The two deployment types of Apache ShardingSphere, JDBC and Proxy, are both tested by Sysbench. ShardingSphere has a newly designed Sysbench-like Java program on the JDBC end that can be used to conduct a pressure test to JDBC and Proxy. It can also ensure that the official Sysbench and our Java program share the same standard.

**Q: Can ShardingSphere converge the connection number?**

A: ShardingSphere-Proxy can converge the connection number, but it will definitely lead to performance loss.

**Q: Can Proxy identify slow SQL?**

A: Currently, the open-source version doesn’t support this function, because most users of the open-source version are Internet enterprises with a low tolerance for performance loss. Thus, the number of probes is relatively small.

**Q: Does [ElasticJob](https://shardingsphere.apache.org/elasticjob/) belong to Apache ShardingSphere?**

A: ElasticJob is currently used as the migration tool by Apache ShardingSphere. Additionally, ElasticJob can also be used for liveness probes.

**Q: Are Internet enterprises using Proxy on a large scale?**

A: Most Internet users choose the mixed deployment model, using JDBC for development and better performance, and Proxy for management. Financial customers prefer to use Proxy because they can take Proxy as a database for unified management without additional learning costs.

**Q: We are currently using ShardingSphere version 4.1.1, what does it support for transactions?**

A: Both versions 4.11 and 5.1.0 support [XA](https://docs.oracle.com/database/121/TTCDV/xa_dtp.htm#TTCDV327) distributed transaction management. We plan to develop the global transaction manager ([GTM](https://docs.oracle.com/cd/E17276_01/html/programmer_reference/xa_build.html)) which is scheduled to start in the second half of this year.

## Get in touch with us
If you have applied Apache ShardingSphere solutions in your business or if you want to quickly understand and introduce the Apache ShardingSphere 5.X ecosystem to your business, you’ll probably like for someone from our community to help you out and share the Apache ShardingSphere technology with your team.

Feel free to reach out to us on one of our official community channels, such as Twitter or Slack.

If we both agree that ShardingSphere is suitable for your business scenarios, our community team will be happy to connect with you and your engineers to take their questions.

**Apache ShardingSphere Project Links:**

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)
