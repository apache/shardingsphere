+++
pre = "<b>3.10. </b>"
title = "测试引擎"
weight = 10
chapter = true
+++

ShardingSphere提供了完善的测试引擎。它以XML方式定义SQL，每条SQL由SQL解析单元测试引擎和整合测试引擎驱动，每个引擎分别为H2、MySQL、PostgreSQL、SQLServer和Oracle数据库运行测试用例。

为了使测试更容易上手，shardingsphere 中的测试引擎不必修改任何 **Java** 代码，只需要修改相应的配置文件即可运行断言。

SQL解析单元测试全面覆盖SQL占位符和字面量维度。整合测试进一步拆分为策略和JDBC两个维度，策略维度包括分库分表、仅分表、仅分库、读写分离等策略，JDBC维度包括Statement、PreparedStatement。

因此，1条SQL会驱动5种数据库的解析 * 2种参数传递类型 + 5种数据库 * 5种分片策略 * 2 种 JDBC 运行方式 = 60个测试用例，以达到ShardingSphere对于高质量的追求。

> 鉴于表述路径时，子路径可能不止一个，名称为某一类术语的集合，这里用 SQL-TYPE 以及 SHARDING-TYPE 表述如下：
>
>SQL-TYPE : 是 DAL，DCL，DDL，DML，DQL，TCL 中的某一个或者集合
>
>SHARDING-TYPE : 是 db，dbtbl_with_primary_replica_replication，primary_replica_replication，tbl 中的某一个或者集合
