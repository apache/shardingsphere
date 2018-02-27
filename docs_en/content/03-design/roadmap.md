+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "Roadmap"
weight = 4
prev = "/03-design/module"
next = "/00-overview"
+++

## Completed

### DATABASE
1. MySQL
1. Oracle
1. SQLServer
1. PostgreSQL

### DQL
1. Simple
1. JOIN
1. BETWEEN
1. IN
1. ORDER BY
1. GROUP BY
1. Aggregation
1. LIMIT, rownum, TOP
1. Simple Sub Query

### DML
1. INSERT INTO
1. INSERT SET
1. UPDATE
1. DELETE

### DDL
1. CREATE
1. ALTER
1. DROP
1. TRUNCATE

### Configuration
1. Java API
1. Spring Namespace
1. Yaml
1. Read/Write Splitting Independent
1. Configuration Concentrated
1. Configuration Dynamic

### SQL Parser
1. lexer analysis
1. Parser analysis

### SQL Rewrite
1. Correctness rewrite
1. polish rewriting

### SQL Router
1. Hint Router
1. Simple Router
1. Cartesian Product Router

### Result Merger
1. Stream Merger
1. Memory Merger
1. Decorator Merger

### Sharding databases and tables
1. sharding databases
1. sharding tables
1. default datasources

### Read/Write Splitting
1. Read/Write Splitting
1. Consistent guarantees for the same thread and the same database connection
1. Hint-based forced master routing

### Distributed Primary Key
1. JDBC integration
1. Distributed primary key policy interface
1. The distributed primary key algorithm based on snowflake is implemented

### B.A.S.E Transaction
1. Best Effort Delivery

## Planning

### Configuration
1. Read/Write Splitting to be independent
1. Binding Strategy improvement
1. centralized
1. dynamic

### Governance
1. HealthCheck and discovery of datasources
1. dynamic switching of datasources
1. flow control

## TODO

### DQL
1. DISTINCT
1. HAVING
1. OR
1. UNION, UNION ALL
1. Calculate Expression, eg: SUM(pv) / COUNT(uv)
1. Complicated Sub Query
1. SQL Hint

### DML
1. INSERT INTO VALUES (xxx), (xxx)
1. UPDATE Multiple Tables
1. DELETE Multiple Tables

### DDL
1. CREATE VIEW
1. CREATE INDEX
1. CREATE OR REPLACE

### Enhanced SQL Parser
1. Batch Parser
1. Redundant Brackets
1. Specify SQL Hint by SQL comments

### Enhanced B.A.S.E Transaction 
1. TCC

### Devops Tools
1. Dictionary table replication broadcasting
1. Dynamic Dilatancy
