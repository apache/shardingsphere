+++
title = "User Norms"
weight = 2
+++

## Supported

* Migrate data outside into databases which managed by Apache ShardingSphere;
* Scale out data between data nodes of Apache ShardingSphere.

## Unsupported

* Scale table without primary key, primary key can not be composite;
* Scale table with composite primary key;
* Do not support scale on in used databases, need to prepare a new database cluster for target.
