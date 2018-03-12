+++
toc = true
title = "Basic Concepts "
weight = 1
prev = "/02-guide"
next = "/02-guide/sharding/"

+++

This section introduces some of the basic concepts for Sharding-JDBC.

## LogicTable
The integrated table name for horizontal-splited tables. e.g. There are ten spliting order tables from t_order_0 to t_order_9, and their logic table name is t_order.

## ActualTable
The physical table really existed in the sharding database. e.g. t_order_0~t_order_9 in the previous example.

## DataNode
The smallest unit of data-sharding. It consists of data source name and table name, e.g. ds_1.t_order_0. By default, the table structure of each sharding is the same, so that you can directly configure the correspondence between logical tables and actual tables. If the table structures in each sharding are different, you can set relationship configuration by using ds.actual_table format.

## BindingTable
The relational tables with the same spliting rules. e.g. The order table splited with Order ID, and the order item table also splited with  ​​Order ID. As a result, order table and order item table are BindingTable of each other. The cascade querie for BindingTables do not use Cartesian product association, therefore the efficiency for cascade query will be greatly improved.

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
