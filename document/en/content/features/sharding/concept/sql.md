+++
toc = true
title = "SQL"
weight = 1
+++

## Logic Table

The integrated table name for horizontal-sharding tables. e.g. There are ten sharding order tables from t_order_0 to t_order_9, and their logic table name is t_order.

## Actual Table

The physical table really existed in the sharding database. e.g. t_order_0~t_order_9 in the previous example.

## Data Node

The smallest unit of data sharding. It consists of data source name and table name, e.g. ds_1.t_order_0. By default, the table structure of each sharding is the same, so that you can directly configure the correspondence between logical tables and actual tables. If the table structures in each sharding are different, you can set relationship configuration by using ds.actual_table format.

## Binding Table

The relational tables with the same sharding rules. e.g. The order table sharding with order_id, and the order item table also sharding with order_id. As a result, order table and order item table are BindingTable of each other. The cascade querie for binding tables do not use Cartesian product association, therefore the efficiency for cascade query will be greatly improved.

## Logic Index

Some databases(such as PostgreSQL) do not permit exist same index name in one database, Some databases(such as MySQL) permit unique indexes name based on tables. Logic index used for first scenario, it need to rewrite index name to index name + table name, the index name before rewrite is called logic index. 
