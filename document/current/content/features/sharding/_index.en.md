+++
pre = "<b>3.1. </b>"
title = "Data sharding"
weight = 1
chapter = true
+++

## Background

The solution to store all the data to a single data node has become more and more difficult to meet the massive data scenarios of the Internet in two aspects of performance and availability.
Most relational databases use the index of the B+ tree. As the amount of data exceeds the threshold, the increase in index depth will increase the IO of disk access, which leads to a decline in query performance. High concurrency requests also make the centralized database a bottleneck for the system.
There are more and more attempts to store data to NoSQL and NewSQL when the traditional relational database is unable to meet the needs. But NoSQL's SQL unfriendliness, NewSQL's immaturity and the imperfections of the ecosystem make them fail to achieve a fatal blow in the game with relational database, and the position of relational database is still unshaken.

In order to solve the performance problem of relational database in mass data processing, data sharding is an effective solution. Split single node data and store them into multi-tables and multi-databases, named sharding.
Sharding can be used to solve performance problems caused by large amount of data. Database sharding can also effectively disperse high concurrency, while table sharding can not ease the amount of concurrency, but it is still possible to use native ACID transactions across the tables. Once across the database, the issues involved in the transaction are very complex.

According to the way of sharding by business, it is called vertical sharding. For example, the user database and order database are split into different databases. Vertical sharding can alleviate the problems caused by the amount of data and the amount of access, but it can not be solved thoroughly. If the count of users and orders after the vertical sharding still exceeds the threshold, the horizontal sharding is required to be further processed.
Split data from a single table into different tables and databases in accordance with certain rules, which is called horizontal sharding. For example, the original order data in the order_ds.t_order table, if sharding in accordance with the user_id, orders will split into 2 datebases, and then split into 4 tables in each database according to the order_id. So the result is order_ds0.t_order0, order_ds0.t_order1, order_ds0.t_order2, order_ds0.t_order3, order_ds1.t_order0, order_ds1.t_order1, order_ds1.t_order2, order_ds1.t_order3.
This is a simple case of horizontal sharding, and it is also very common to make the sharding of the database and table more dispersed in practical use.

While data sharding solves the performance problem, additional problems have been involved. In the face of so scattered sharding data, developers and ops will meet great challenge. They need to know the actual database and table that store the data. **The objective of data sharding middleware is to eliminate the influence caused by sharding, and allow user to use sharding database like a single database.**
