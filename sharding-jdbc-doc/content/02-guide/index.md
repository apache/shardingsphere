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

本章节最重要将详细介绍如何使用`Sharding-JDBC`

 - 想一览基本的概念，请阅读[基本概念](/02-guide/concepts)。
 - 想通过一个完整的例子使用`Sharding-JDBC`进行分库分表，请阅读[如何分库分表](/02-guide/sharding)章节。
 - 一般情况`Sharding-JDBC`是通过`SQL解析`来获取数据路由数据的，同时你可以使用[Hint](/02-guide/hint-sharding-value)方式来编程设置该数据。该模式非常适合存量系统扩容。
 - [使用配置文件](/02-guide/configuration)介绍了如何利用配置文件来使用`Sharding-JDBC`，配置文件支持`YAML`和`Spring`两种方式。
 - 分库分表后主键如何生成？[分布式Id生成器](/02-guide/id-generator)将给你答案。
 - 分库分表后，要进一步提高性能，可以使用[读写分离](/02-guide/master-slave)功能。
 - 分库后事务如何保证？[事务说明](/02-guide/transaction)章节将告诉你我们的回答。如果你认同我们的理念，[柔性事务](/02-guide/soft-transaction)将告诉你如何使用这种`Sharding-JDBC`特有的事务模式。`
