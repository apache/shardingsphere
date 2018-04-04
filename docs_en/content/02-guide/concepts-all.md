+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "Basic Concepts "
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

## BindingTable
指在任何场景下分片规则均一致的主表和子表。例：订单表和订单项表，均按照订单ID分片，则此两张表互为BindingTable关系。BindingTable关系的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升。

## ShardingColumn
分片字段。用于将数据库(表)水平拆分的关键字段。例：订单表订单ID分片尾数取模分片，则订单ID为分片字段。SQL中如果无分片字段，将执行全路由，性能较差。Sharding-JDBC支持多分片字段。

## ShardingAlgorithm
分片算法。Sharding-JDBC通过分片算法将数据分片，支持通过等号、BETWEEN和IN分片。分片算法目前需要业务方开发者自行实现，可实现的灵活度非常高。未来Sharding-JDBC也将会实现常用分片算法，如range，hash和tag等。

## SQL Hint
对于分片字段非SQL决定，而由其他外置条件决定的场景，可使用SQL Hint灵活的注入分片字段。例：内部系统，按照员工登录ID分库，而数据库中并无此字段。SQL Hint支持通过ThreadLocal和SQL注释(待实现)两种方式使用。

## Config Map
通过ConfigMap可以配置分库分表或读写分离数据源的元数据，可通过调用ConfigMapContext.getInstance()获取ConfigMap中的shardingConfig和masterSlaveConfig数据。例：如果机器权重不同则流量可能不同，可通过ConfigMap配置机器权重元数据。

## LogicIndex
数据分片的逻辑索引名称，DDL语句中水平拆分的表，同一类表的总称。例：订单数据根据主键尾数拆分为10张表,分别是t_order_0到t_order_9，他们的逻辑表名为t_order，对于DROP INDEX t_order_index语句，需在TableRule中配置逻辑索引t_order_index。

This section introduces some of the basic concepts for Sharding-JDBC.

## LogicTable
The integrated table name for horizontal-splited tables. e.g. There are ten spliting order tables from t_order_0 to t_order_9, and their logic table name is t_order.

## ActualTable
The physical table really existed in the sharding database. e.g. t_order_0~t_order_9 in the previous example.

## DataNode
The smallest unit of data-sharding. It consists of data source name and table name, e.g. ds_1.t_order_0. By default, the table structure of each sharding is the same, so that you can directly configure the correspondence between logical tables and actual tables. If the table structures in each sharding are different, you can set relationship configuration by using ds.actual_table format.

## BindingTable
The parent tables and child tables with the same spliting rules. e.g. The order table splited with Order ID, and the order item table also splited with  ​​Order ID. As a result, order table and order item table are BindingTable of each other. The cascade querie for BindingTables do not use Cartesian product association, therefore the efficiency for cascade query will be greatly improved.

## ShardingColumn
The spliting field. e.g. To split tables by using modulo operation for the mantissa of the order ID, the ShardingColumn is order ID. Sharding-JDBC supports multiple sharding columns. Notice: If there is no ShardingColumn in query SQL, all tables will be accessed and result in poor performance.

## ShardingAlgorithm
Sharding-JDBC splits the data by the sharping algorithm, supporting =, BETWEEN and IN algorithms. ShardingAlgorithm currently need to be implemented by users. In the near future, Sharding-JDBC will also carry out some common sharding algorithms such as range, hash and tag.

## SQL Hint
In some cases that ShardingColumn is decided by business conditions, not by certain SQL, then you can use SQL Hint to flexibly achieve injection of ShardingColumn. e.g. If you want to split database according to the employees' ID, but ID column not exists in tables, then you can use SQL Hint to do data sharding. ThreadLocal or SQL annotations(TO DO) method can be used to make SQL Hint.

## Config Map
ConfigMap allows you to configure metadata information for data source of Sharding or Read-write splitting. The information of shardingConfig and masterSlaveConfig in ConfigMap can be obtained by calling ConfigMapContext.getInstance (). e.g. Differet weight for machines, different traffic on machines. The metadata for machines' weight can be configured through the ConfigMap.

## LogicIndex
The logical index name for LogicTable, not for ActualTable. e.g. The index named t_order_index existed in LogicTable 't_order' in previous example. You need to configure LogicIndex in TableRule to execute 'DROP INDEX t_order_index' SQL.
