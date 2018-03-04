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

 - To take a quick look on basic concepts, please read [Basic Concepts] (/ 02-guide / concepts /).
 - A detailed example is given in section [Database Sharding] (/ 02-guide / sharding /) to introduce how to use Sharding-JDBC to shard database or table.
 - To further improve performance after sharding, you can read [Read-write splitting] (/ 02-guide / master-slave /).
 - In consideration of flexible and complex Sharding-JDBC configuration, [Domain Model Configuration] (/ 02-guide / config_domain /) clearly shows you the relationships among the various configuration classes.
 - [Configuration Manual] (/ 02-guide / configuration /) describes how to configure some items with JAVA, YAML, Spring or Spring Boot.
 - To manage the access to databases, please read [Orchestration] (/ 02-guide / orchestration /).
 - Generally Sharding-JDBC obtains sharded data through SQL parsing, but the data can also be got using the [Mandatory Routing] (/ 02-guide / hint-sharding-value /). You perfer to choose this strategy for the expansion of existing system.
 - How to generate the Distributed primary key? [Distributed Primary key] (/ 02-guide / key-generator /) will give you the answer.
 - How to implement transaction support? [Transaction Support] (/ 02-guide / transaction /) describes how Sharding-JDBC implements distributed transactions by using B.A.S.E Transaction.
 - Will the pagination affect the performance? How to deal with the subquery SQL for pagination in Oracle or SQLServer? Please read [Pagination & Subqueries] (/ 02-guide / subquery /).
 - How does Sharding-JDBC run unit testing and integration testing? You can get answer in [Test Engine] (/ 02-guide / test-framework /).
 - Do you want to find performance problems? Please refer to the recommendations in the [Application Performance Monitoring (APM)] (/ 02-guide / apm /).