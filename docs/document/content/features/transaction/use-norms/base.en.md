+++
title = "BASE"
weight = 3
+++

## Supported

* Support cross-database transactions after sharding;
* Support RC isolation level;
* Rollback transaction according to undo log;
* Support recovery committing transaction automatically after the service is down.

## Unsupported

* Do not support other isolation level except RC.

## To Be Optimized

* SQL parsed twice by Apache ShardingSphere and SEATA.
