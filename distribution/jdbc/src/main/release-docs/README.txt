Welcome to Apache ShardingSphere
===============================================================================

Apache ShardingSphere is positioned as a Database Plus, and aims at building a standard layer and ecosystem above heterogeneous databases. 
It focuses on how to reuse existing databases and their respective upper layer, rather than creating a new database.
The goal is to minimize or eliminate the challenges caused by underlying databases fragmentation.

The concepts at the core of the project are Connect, Enhance and Pluggable.

- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage. It can quickly connect applications and heterogeneous databases quickly.
- `Enhance:` Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance (circuit breaker and access limitation and analyze, QoS and observability).
- `Pluggable:` Leveraging the micro kernel and 3 layers pluggable mode, features and database ecosystem can be embedded flexibily. Developers can customize their ShardingSphere just like building with LEGO blocks.

Apache ShardingSphere including 2 independent products: JDBC & Proxy.
They all provide functions of data scale-out, distributed transaction and distributed governance,
applicable in a variety of situations such as Java isomorphism, heterogeneous language and Cloud-Native.

As the cornerstone of enterprises, the relational database has a huge market share.
Therefore, we prefer to focus on its incrementation instead of a total overturn.

ShardingSphere-JDBC is a lightweight Java framework that provides additional services at Java's JDBC layer.
With the client connecting directly to the database, it provides services in the form of jar and requires no extra deployment and dependence.
It can be considered as an enhanced version of the JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

- Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template, or direct use of JDBC;
- Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, HikariCP;
- Support any kind of JDBC standard database: MySQL, PostgreSQL, Oracle, SQLServer and any JDBC adapted databases.

ShardingSphere-JDBC is suitable for java applications.

Getting Started
===============================================================================
To help you get started, try the following links:

Getting Started
    https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-jdbc-quick-start/

We welcome contributions of all kinds, for details of how you can help
    https://shardingsphere.apache.org/community/en/involved/

Find the issue tracker from here
    https://github.com/apache/shardingsphere/issues

Please help us make Apache ShardingSphere better - we appreciate any feedback you may have.

Have fun!

-----------------

Licensing
===============================================================================

This software is licensed under the terms you may find in the file named "LICENSE" in this directory.
