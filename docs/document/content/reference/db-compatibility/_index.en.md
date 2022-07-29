+++
pre = "<b>7.1. </b>"
title = "Database Compatibility"
weight = 1
+++

![The core of SQL parsing: Abstract Syntax Tree (AST)](https://shardingsphere.apache.org/document/current/img/db-compatibility/principle1.png)

- SQL compatibility

SQL is the standard language for users to communicate with databases. The SQL parsing engine is responsible for parsing SQL strings into abstract syntax trees so that Apache ShardingSphere can understand and implement its incremental function.
ShardingSphere currently supports MySQL, PostgreSQL, SQLServer, Oracle, openGauss, and SQL dialects conforming to the SQL92 standard. Due to the complexity of SQL syntax, a few SQL are not supported for now.

- Database protocol compatibility

Apache ShardingSphere currently implements MySQL and PostgreSQL protocols according to different data protocols.

- Supported features

Apache ShardingSphere provides distributed collaboration capabilities for databases. At the same time, it abstracts some database features to the upper layer for unified management, so as to facilitate users.

Therefore, native SQL will not deliver the features provided uniformly to the database, and a message will be displayed indicating that the operation is not supported. Users can replace it with methods provided by ShardingSphere.
