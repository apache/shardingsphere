+++
toc = true
title = "Test Engine"
weight = 8
+++

ShardingSphere has provided a full test engine, which defines SQL in the form of XML. 
Each SQL is driven by an SQL parsing unit test engine and an integration test engine, while each engine runs test cases for H2, MySQL, PostgreSQL, SQLServer and Oracle databases separately.

The SQL parsing unit test covers both SQL placeholder and literal dimension. 
Integration test can be further divided into two dimensions of strategy and JDBC; the former one includes strategies as Sharding, table Sharding, database Sharding, and read-write split while the latter one includes `Statement` and `PreparedStatement`.

Therefore, one SQL can drive 5 kinds of database parsing * 2 kinds of parameter transmission modes + 5 kinds of databases * 5 kinds of Sharding strategies * 2 kinds of JDBC operation modes = 60 test cases, to enable ShardingSphere to achieve the pursuit of high quality.

# Integration Test

## Test Environment

Since the actual database environment is involved in the integrated test, the following preparation work should be finished before the test:

1. Run `resources/integrate/schema/manual_schema_create.sql` in the database to be tested. Create databases (MySQL, PostgreSQL and SQLServer) and Schema (only Oracle).

1. Modify databases in `sharding-jdbc/src/test/resources/integrate/env.properties` to appoint the test database to be tested.

1. Run `AllIntegrateTests` to test the result.

## Notice

1. If Oracle needs to be tested, please add Oracle driver dependencies to the pom.xml.
1. 10 splitting-databases and 10 splitting-tables are used in the integrated test to ensure the test data is full, so it will take a relatively long time to run the test cases.

# SQL Parsing Engine Test

## Test Environment

SQL Parsing Engine Test is the test based on SQL itself, so AllParsingTests can be run directly without connecting to the database.
