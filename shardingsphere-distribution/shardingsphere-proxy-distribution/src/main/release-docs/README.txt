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

ShardingSphere-Proxy is a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages.
Currently, MySQL and PostgreSQL protocols are provided.
It can use any kind of terminal that is compatible with MySQL or PostgreSQL protocol to operate data, which is more friendly to DBAs.

- Transparent to applications, it can be used directly as MySQL/PostgreSQL;
- Compatible with MySQL-based databases, such as MariaDB, and PostgreSQL-based databases, such as openGauss;
- Applicable to any kind of client that is compatible with MySQL/PostgreSQL protocol, such as MySQL Command Client, MySQL Workbench, etc.

The advantages of ShardingSphere-Proxy lie in supporting heterogeneous languages and providing operational entries for DBA.

Getting Started
===============================================================================
To help you get started, try the following links:

Getting Started
    https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/

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
