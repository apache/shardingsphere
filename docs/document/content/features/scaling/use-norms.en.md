+++
title = "User Norms"
weight = 2
+++

## Supported

* Migrate out data into databases which managed by Apache ShardingSphere;
* Scale out data between data nodes of Apache ShardingSphere.

## Unsupported

* Do not support to scale tables without primary key which must be single column.
* Do not support scale on database cluster which is in use on proxy, need to prepare a new database cluster for scaling target.
