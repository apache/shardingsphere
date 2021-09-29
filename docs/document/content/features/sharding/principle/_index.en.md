+++
pre = "<b>3.1.2. </b>"
title = "Guide to Kernel"
weight = 2
chapter = true
+++

ShardingSphere 的 3 个产品的数据分片主要流程是完全一致的，按照是否进行查询优化，可以分为 Standard 内核流程和 Federation 执行引擎流程。
Standard 内核流程由 `SQL 解析 => SQL 路由 => SQL 改写 => SQL 执行 => 结果归并` 组成，主要用于处理标准分片场景下的 SQL 执行。
Federation 执行引擎流程由 `SQL 解析 => 逻辑优化 => 物理优化 => 优化执行 => Standard 内核流程` 组成，Federation 执行引擎内部会进行逻辑优化和物理优化，在优化执行阶段会依赖 Standard 内核流程，对优化后的逻辑 SQL 进行改写、执行和归并。

The major sharding processes of all the three ShardingSphere products are identical. According to whether query optimization is performed, they can be divided into standard kernel process and federation executor engine process.
The standard kernel process consists of `SQL Parse => SQL Route => SQL Rewrite => SQL Execute => Result Merge`, which is used to process SQL execution in standard sharding scenarios.
The federation executor engine process consists of `SQL Parse => Logical Plan Optimize => Physical Plan Optimize => Plan Execute => Standard Kernel Process`. The federation executor engine perform logical plan optimization and physical plan optimization. In the optimization execution phase, it relies on the standard kernel process to rewrite, execute, and merge the optimized logical SQL.

![Sharding Architecture Diagram](https://shardingsphere.apache.org/document/current/img/sharding/sharding_architecture_en.png)

## SQL Parsing

It is divided into lexical parsing and syntactic parsing. The lexical parser will split SQL into inseparable words, and then the syntactic parser will analyze SQL and extract the parsing context, which can include tables, options, ordering items, grouping items, aggregation functions, pagination information, query conditions and placeholders that may be revised.

## SQL Route

It is the sharding strategy that matches users’ configurations according to the parsing context and the route path can be generated. It supports sharding route and broadcast route currently.

## SQL Rewrite

It rewrites SQL as statement that can be rightly executed in the real database, and can be divided into correctness rewrite and optimization rewrite.

## SQL Execution

 Through multi-thread executor, it executes asynchronously.

## Result Merger

It merges multiple execution result sets to output through unified JDBC interface. Result merger includes methods as stream merger, memory merger and addition merger using decorator merger.

## Query Optimization

Supported by federation executor engine(under development), query optimization is performed on complex query such as join query and subquery. It also supports distributed query across multiple database instances. It uses relational algebra internally to optimize query plan, and then get query result through the best query plan.
