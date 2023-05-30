+++
title = "Limitations"
weight = 2
+++

## Procedures Supported

* Migration of peripheral data to databases managed by Apache ShardingSphere.
* Target proxy without rule or configure any rule.
* Migration of single column primary key or unique key table, the first column type could be: integer data type, string data type and part of binary data type (e.g. MySQL VARBINARY).
* Migration of multiple column primary keys or unique keys table.

## Procedures not supported

* Migration on top of the current storage node is not supported, so a brand new database cluster needs to be prepared as the migration target cluster.
* Target proxy table rule contains HINT strategy.
* Use different target table schema from source table schema.
* Source table DDL changes during migration.
