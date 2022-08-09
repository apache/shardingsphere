+++
pre = "<b>3.5. </b>"
title = "DB Gateway"
weight = 5
chapter = true
+++

## Definition

As database fragmentation is an irreversible trend, the coexistence of multiple types of databases has been normal. An increasing number of scenarios use an SQL dialect to access heterogeneous databases. Diversified databases make it difficult to standardize SQL dialects for accessing databases. Engineers need to use different dialects for different kinds of databases, and a unified query platform is needed.

The database gateway can automatically translate different types of database dialects into the dialects used by the back-end database, making it much easier for users to use the underlying heterogeneous database.

## Related Concepts

### SQL Dialect

SQL dialect means database dialect, and it indicates that some database projects have their own unique syntax in addition to SQL, which are also called dialects. Different database projects may have different SQL dialects.

## Impact on the System

Through database gateway, engineers can use any database dialect to access all back-end heterogeneous databases, which can greatly reduce development and maintenance costs.

## Limitations

The SQL dialect translation of Apache ShardingSphere is experimental.

Currently, only MySQL/PostgreSQL dialects can be automatically translated. Engineers can use MySQL dialects and protocols to access PostgreSQL databases and vice versa.

## Related References

Source Codes: https://github.com/apache/shardingsphere/tree/master/shardingsphere-kernel/shardingsphere-sql-translator
