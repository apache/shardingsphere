+++
toc = true
title = "Process"
weight = 4
+++

Sharding-Sphere's 3 projects have same main process on data sharding. They are SQL Parser => Executor Optimizer => SQL Router => SQL Rewrite => SQL Execution => Result Merger, and using the logical table which the user configured, the real SQL is completely shielded from the database access.

![Sharding Architecture Diagram](http://ovfotjrsi.bkt.clouddn.com/sharding_architecture_en.png)


## SQL Parser

It is divided into lexical parser and syntactic parser. First of all, the SQL is split into some separate words by lexical parser. The syntactic parser is used to understand the SQL, and then the context is finally extracted. Parsing context includes tables, selections, sorting items, grouping items, aggregate functions, paging information, query conditions, and placeholders that may need to be modified.

## Executor Optimizer

Merge and optimize sharding conditions, such as OR.

## SQL Router

The sharding strategy by the user configuration is matched based on the parsing context, and the routing path is generated. It currently supports sharding routing, Hint routing, broadcast routing, unicast routing, and blocking routing.
Sharding routing is used to routing SQL with sharding keys. According to the difference of sharding keys, it can be divided into single key routing (sharding operators is '='), multiple keys routing (sharding operators is 'IN') and range routing (sharding operators is 'BETWEEN').
Hint routing is used as a route to the way the destination is injected through a program, and it can be used when the sharding information is not included in the SQL.
Broadcast routing is used for scenes that do not contain sharding keys in SQL. According to the SQL type, it can be divided into all databases broadcast routing (SET AUTOCOMMIT=1) and all databases and tables broadcast routing (DQL, DML, DDL).
Unicast routing is used to obtain information of a actual table, such as DESCRIBE table_name.
Block routing is used to block the operation of SQL to the database, such as USE db_name, because Sharding-Sphere has only one logical data source and does not need to switch.

## SQL Rewrite

Rewrite the SQL as a statement that can be executed correctly in a real database. SQL rewriting is divided into correctness  rewriting and optimization rewriting.
Correctness rewriting includes replacing the name of logical table with the name of real table, rewrite the initial value and ending value of paging information, add complement column of sorting, grouping and increasing primary key, and rewriting AVG to SUM / COUNT.
Optimization rewriting is able to rewrite SQL, which is more suitable for executing in distributed database. For example, add sorting column to SQLs which contain only GROUP BY operator, so that group merging can be converted from memory merging to flow merging.

## SQL Execution

Asynchronous execution is performed through multithreading executor, but the SQL of different tables of the same physical data source will use the same thread of the same connection to ensure the integrity of its transaction.

## Result Merger

Multiple execution result sets are incorporated in order to facilitate the output of a unified JDBC interface. The result is the merging of the flow merging, the memory merging, and the addition of the decorator pattern merging.
Flow merging is used for simple queries, sorting queries, grouping queries, sorting and grouping, but sorting items and grouping items are exactly the same. The way of traversing the result set of flow merging is getting data by calling next method every time without additional memory.
Memory merging is only used for scenes that are inconsistent with sorting items and grouping items. It is necessary to load all the data in the result to memory. If the result set is too large, it will occupy a lot of memory.
Using decorator pattern merging for paging, whether it is simple query, sorting query or group query, paging SQL will merge through paging decorator to process paging related results.
