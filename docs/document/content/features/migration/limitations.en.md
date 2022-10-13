+++
title = "Limitations"
weight = 2
+++

## Procedures Supported

* Migration of peripheral data to databases managed by Apache ShardingSphere.
* Migration of integer or string unique key tables.
* Migration of integer or string primary key tables.

## Procedures not supported

* Migration without primary key and unique key tables.
* Migration of composite primary key or composite unique key tables.
* Migration on top of the current storage node is not supported, so a brand new database cluster needs to be prepared as the migration target cluster.
