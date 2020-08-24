+++
pre = "<b>3.1.2. </b>"
title = "Guide to Kernel"
weight = 2
chapter = true
+++

The major sharding processes of all the three ShardingSphere products are identical. The core consists of `SQL parsing => query optimization => SQL route => SQL rewrite => SQL execution => result merger`.

![Sharding Architecture Diagram](https://shardingsphere.apache.org/document/current/img/sharding/sharding_architecture_en.png)

## SQL Parsing

It is divided into lexical parsing and syntactic parsing. The lexical parser will split SQL into inseparable words, and then the syntactic parser will analyze SQL and extract the parsing context, which can include tables, options, ordering items, grouping items, aggregation functions, pagination information, query conditions and placeholders that may be revised.

## Query Optimization

It merges and optimizes sharding conditions, such as OR.

## SQL Route

It is the sharding strategy that matches users’ configurations according to the parsing context and the route path can be generated. It supports sharding route and broadcast route currently.

## SQL Rewrite

It rewrites SQL as statement that can be rightly executed in the real database, and can be divided into correctness rewrite and optimization rewrite.

## SQL Execution

 Through multi-thread executor, it executes asynchronously.

## Result Merger

It merges multiple execution result sets to output through unified JDBC interface. Result merger includes methods as stream merger, memory merger and addition merger using decorator merger.
