+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "Feature List"
weight = 4
prev = "/01-start/faq/"
next = "/01-start/limitations/"

+++

## The functional requirement

### Data Sharding 
* Perfect SQL parsing, Comprehensive SQL queries for aggregation/grouping/sorting/LIMIT/TOP and cascade/Cartesian-product.
* Support for internal or external join quiries.
* Flexible Sharding strategy. Support =, BETWEEN, IN, other multi-dimensional Sharding or user-defined Sharding strategy. Support the sharing of Sharding column.
* Hint-based mandatory Sharding routing.

### Read-write splitting
* Support SQL Passthrough by using independent Read-write splitting
* Support Read-write splitting configuration for single-master and multi-slaves
* Hint-based mandatory master-database routing

### B.A.S.E transaction
* Best efforts delivery transaction.
* Try confirm cancel transaction (TBD).

### Distributed primary key
* unified time-based distributed ID generator.

### The compatibility 
* Support using any java-based ORM framework, such as: JPA, Hibernate, Mybatis, Spring JDBC Template or directly using JDBC
* Support using any third party database connection pool, such as: DBCP, C3P0, BoneCP, Druid, etc.
* Support the database that implements the JDBC interface. Currently supports MySQL, Oracle, SQLServer and PostgreSQL

### The flexible configuration
* Java
* YAML
* Inline expression
* Spring namespace
* Spring boot starter

### The orchestration ability (2.0 new feature)
* The centralized and dynamic configuration can support the dynamic strategy switching of Sharding and read-write splitting (2.0.0.M1).
* The orchestration for databases, and the auto switch for unreachable databases (2.0.0.M2).
* APM information output based on Open Tracing protocol (2.0.0.m3).


## The performance
1. The SQL parsing engine bases on "half-understanding".
2. To merge for Multithreaded processing results.
3. The performance loss rate is about 6%.


## The stability 
1. The perfect fatigue test, and regular queries without Full GC (except for GROUP BY)
2. The complete unit testing of multiple databases, Sharding strategies and SQLs.

