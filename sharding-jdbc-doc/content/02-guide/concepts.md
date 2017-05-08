+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "核心概念"
weight = 1
prev = "/02-guide"
next = "/02-guide/sharding/"

+++

本文介绍Sharding-JDBC包含的一些核心概念。

## LogicTable
数据分片的逻辑表，对于水平拆分的数据库(表)，同一类表的总称。例：订单数据根据主键尾数拆分为10张表,分别是t_order_0到t_order_9，他们的逻辑表名为t_order。

## ActualTable
在分片的数据库中真实存在的物理表。即上个示例中的t_order_0到t_order_9。

## DataNode
数据分片的最小单元。由数据源名称和数据表组成，例：ds_1.t_order_0。配置时默认各个分片数据库的表结构均相同，直接配置逻辑表和真实表对应关系即可。如果各数据库的表结果不同，可使用ds.actual_table配置。

## DynamicTable
逻辑表和真实表不一定需要在配置规则中静态配置。比如按照日期分片的场景，真实表的名称随着时间的推移会产生变化。此类需求Sharding-JDBC是支持的，不过目前配置并不友好，会在新版本中提升。

## BindingTable
指在任何场景下分片规则均一致的主表和子表。例：订单表和订单项表，均按照订单ID分片，则此两张表互为BindingTable关系。BindingTable关系的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升。

## ShardingColumn
分片字段。用于将数据库(表)水平拆分的关键字段。例：订单表订单ID分片尾数取模分片，则订单ID为分片字段。SQL中如果无分片字段，将执行全路由，性能较差。Sharding-JDBC支持多分片字段。

## ShardingAlgorithm
分片算法。Sharding-JDBC通过分片算法将数据分片，支持通过等号、BETWEEN和IN分片。分片算法目前需要业务方开发者自行实现，可实现的灵活度非常高。未来Sharding-JDBC也将会实现常用分片算法，如range，hash和tag等。

## SQL Hint
对于分片字段非SQL决定，而由其他外置条件决定的场景，可使用SQL Hint灵活的注入分片字段。例：内部系统，按照员工登录ID分库，而数据库中并无此字段。SQL Hint支持通过ThreadLocal和SQL注释(待实现)两种方式使用。
