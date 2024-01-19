+++
pre = "<b>3.11. </b>"
title = "SQL Federation"
weight = 11
chapter = true
+++

## Background

When data is sharded and stored in multiple database instances, although it can effectively address database performance bottlenecks, it also brings some new problems in business implementation.

For example, in the following scenarios: cross database association queries, sub queries, pagination, sorting, aggregation queries.

When implementing business operations, it is important to pay attention to the usage range of SQL queries and avoid cross database instance queries as much as possible, which limits the functionality of the business at the database level.

## Challenges

User business queries in SQL are often complex and variable, and it is costly to integrate them into ShardingSphere through business SQL transformation.

Convert the original queries on the business side into distributed queries and perform corresponding SQL optimization in distributed query scenarios, which can be completed across database instances: associated queries, sub queries, pagination, sorting, and aggregation queries.

In terms of business implementation, it can enable R&D personnel to no longer care about the scope of SQL usage, focus on business function development, and reduce functional limitations at the business level.

## Goal

Implementing distributed SQL for cross database instance queries is the main design goal of Apache ShardingSphere federated queries.

## Application Scenario

When cross database association queries, sub queries, and aggregate queries are required. No need to modify SQL, enabling federated queries through configuration can complete the execution of distributed query statements.

## Related References

- [SQL Federation Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-federation/)
