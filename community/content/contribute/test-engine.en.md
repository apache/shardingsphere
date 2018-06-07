+++
toc = true
title = "Test Engine"
weight = 6
+++

Sharding-Sphere provides a perfect test engine. It defines SQL in the form of XML, and each of SQL is driven by an SQL parsing unit test engine and an integration test engine, each of which provides test cases for H2, MySQL, PostgreSQL, SQL server, and Oracle databases.
The SQL unit test covers SQL placeholders and literal dimensions, and the integration test consists of strategy and JDBC. The strategy in integration test is made up of Sharding, table Sharding, database Sharding, and a Read-write splitting, and the JDBC is made up of Statement and PreparedStatement.
Therefore, a SQL will drive 5 kinds of databases * 2 kinds of JDBC operation modes + 5 kinds of strategies * 5 kinds of databases * 2 kinds of JDBC operation modes = 60 test cases, in order to achieve high test standard for Sharding-Sphere.

# Integration test

## Test environment

You need to prepare database environment before performing an integration test:

1. Run resources/integrate/schema/manual_schema_create.sql in test database environment to create database (MySQL、PostgreSQL、SQLServer) and Schema（ Only for Oracle）.

1. Modify databases in sharding-jdbc/src/test/resources/integrate/env.properties to specify the test databases.

1. Run All integration tests to get test results.

## Notices

1. To test Oracle, please add Oracle driver dependencies to the POM file.
1. In order to ensure the integrity of the test data, we use 10 splitting-databases and 10 splitting-tables to execute the integration test of Sharding. Therefore it will take a long time to run the test cases.

# The engine test of SQL parsing

## Test environment

It is based on SQL parsing, so you do not need to connect to the database and run AllParsingTests directly.
