+++
pre = "<b>3.1.1. </b>"
title = "Guide to Kernel"
weight = 2
chapter = true
+++

The major sharding processes of all the three ShardingSphere products are identical. 
The core is consist of `SQL parsing => query optimization => SQL route => SQL rewrite => SQL execution => result merger`.

![Sharding Architecture Diagram](https://shardingsphere.apache.org/document/current/img/sharding/sharding_architecture_en.png)

## SQL Parsing

It is divided into lexical parser and syntactic parser. 
The lexical parser will separate SQL into inseparable words, and then syntactic parser will understand SQL and extract the context. 
Tables, selecting items, ordering items, grouping items, aggregation functions, pagination information, query conditions and placeholders that may be revised are all included in the parsing context.

## Query optimization

It merges and optimizes sharding conditions, such as OR.

## SQL Route

It is the sharding strategy that matches users’ configurations according to the parsing context and generates route path. 
It supports sharding route and broadcast route currently.

## SQL Rewrite

It rewrites SQL as statement that can be rightly executed in the real database, and can be divided into correctness rewrite and optimization rewrite.

## SQL Execution

It executes asynchronously through multi-threading executor.

## Result Merging

It merges multiple execution result sets to output through unified JDBC interface. 
Result merger includes methods as stream merger, memory merger and addition merger using decorator merger.
