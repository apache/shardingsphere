+++
icon = "<b>2. </b>"
date = "2016-12-12T16:06:17+08:00"
title = "User Manual"
weight = 0
prev = "/01-start/stress-test"
next = "/02-guide/concepts"
chapter = true

+++

# The Chapter Introduction

 - 想一览基本的概念，请阅读[基本概念](/02-guide/concepts/)。
 - 想通过一个完整的例子使用Sharding-JDBC进行分库分表，请阅读[如何分库分表](/02-guide/sharding/)章节。
 - 分库分表后，要进一步提高性能，可以使用[读写分离](/02-guide/master-slave/)功能。
 - Sharding-JDBC的配置灵活而复杂，[域模型配置](/02-guide/config_domain/)清晰的展现了各个配置类间的关系。
 - [配置手册](/02-guide/configuration/)介绍了如何通过JAVA,YAML,Spring和Spring Boot四种方式配置。
 - 客户端的数据库治理，需要[编排治理](/02-guide/orchestration/)功能来实现。
 - 一般情况Sharding-JDBC是通过SQL解析来获取数据路由数据的，但也可以使用[强制路由](/02-guide/hint-sharding-value/)方式来设置分片数据。该模式非常适合存量系统扩容。
 - 分库分表后主键如何生成？[分布式主键](/02-guide/key-generator/)将给你答案。
 - 分库后事务如何保证？[事务支持](/02-guide/transaction/)将介绍Sharding-JDBC如何通过使用柔性事务的方式来实现分布式事务。
 - 分页是否影响性能？Oracle和SQLServer的分页涉及到子查询又该如何处理？请阅读[分页及子查询](/02-guide/subquery/)。
 - Sharding-JDBC如何运行单元测试和整合测试？请阅读[测试引擎](/02-guide/test-framework/)。
 - 想定位性能问题？可以参考[应用性能监控(APM)](/02-guide/apm/)部分给出的建议。


 - To take a quick look on basic concepts, please read [Basic Concepts] (/ 02-guide / concepts /).
 - A detailed example is given in section [Database-Sharding] (/ 02-guide / sharding /) to introduce how to use Sharding-JDBC to shard database or table.
 - To further improve performance after sharding, you can read [(Read-write splitting] (/ 02-guide / master-slave /) function.
 - In consideration of flexible and complex Sharding-JDBC configuration, [Domain Model Configuration] (/ 02-guide / config_domain /) clearly shows you the relationships among the various configuration classes.
 - [Configuration] (/ 02-guide / configuration /) describes how to configure some items with JAVA, YAML, Spring or Spring Boot.
 - To manage the access to databases, please read [DB-access Management] (/ 02-guide / orchestration /).
 - Generally Sharding-JDBC obtains sharded data through SQL parsing, but the data can also be got using the [Mandatory routing] (/ 02-guide / hint-sharding-value /). You perfer to choose this strategy for the expansion of existing system.
 - How to generate the Distributed primary key? [Distributed Primary key] (/ 02-guide / key-generator /) will give you the answer.
 - How to implement transaction support? [Transaction Support] (/ 02-guide / transaction /) describes how Sharding-JDBC implements distributed transactions by using B.A.S.E Transaction.
 - Will pagination affect the performance? How to deal with the subquery SQL for pagination in Oracle or SQLServe? Please read [Pagination & Subqueries] (/ 02-guide / subquery /).
 - How does Sharding-JDBC run unit testing and integration testing? You can get answer in [Test Engine] (/ 02-guide / test-framework /).
 - Do you want to find performance problems? Please refer to the recommendations in the [Application Performance Monitoring (APM)] (/ 02-guide / apm /).