+++
icon = "<b>2. </b>"
date = "2016-12-12T16:06:17+08:00"
title = "使用指南"
weight = 0
prev = "/01-start/stress-test"
next = "/02-guide/concepts"
chapter = true

+++

# 本章导航

 - 想一览基本的概念，请阅读[基本概念](/02-guide/concepts/)。
 - 想通过一个完整的例子使用Sharding-JDBC进行分库分表，请阅读[如何分库分表](/02-guide/sharding/)章节。
 - 分库分表后，要进一步提高性能，可以使用[读写分离](/02-guide/master-slave/)功能。
 - [配置手册](/02-guide/configuration/)介绍了如何通过YAML和Spring两种方式配置。
 - 一般情况Sharding-JDBC是通过SQL解析来获取数据路由数据的，但也可以使用[强制路由](/02-guide/hint-sharding-value/)方式来设置分片数据。该模式非常适合存量系统扩容。
 - 分库分表后主键如何生成？[分布式主键](/02-guide/id-generator/)将给你答案。
 - 分库后事务如何保证？[事务支持](/02-guide/transaction/)将介绍Sharding-JDBC如何通过使用柔性事务的方式来实现分布式事务。
