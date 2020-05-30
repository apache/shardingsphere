+++
pre = "<b>3.6.3. </b>"
title = "Use Norms"
weight = 3
+++

## Supported Items

* The back-end databases are MySQL, Oracle, PostgreSQL, and SQLServer;
* The user needs to encrypt one or more columns in the database table (data encryption & decryption);
* Compatible with all commonly used SQL.

## Unsupported Items

* Users need to deal with the original inventory data and wash numbers in the database;
* Use encryption function + sub-library sub-table function, some special SQL is not supported, please refer to [SQL specification]( https://shardingsphere.apache.org/document/current/en/features/sharding/use-norms/sql/);
* Encryption fields cannot support comparison operations, such as: greater than less than, ORDER BY, BETWEEN, LIKE, etc;
* Encryption fields cannot support calculation operations, such as AVG, SUM, and calculation expressions.
