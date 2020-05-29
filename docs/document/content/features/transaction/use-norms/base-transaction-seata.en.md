+++
title = "Seata BASE transaction"
weight = 3
+++

## Supported Items

* Support cross-database transactions after sharding;
* Support RC isolation level;
* Rollback transaction according to undo log;
* Support recovery committing transaction automatically after the service is down.

## Unsupported Items

* Do not support other isolation level except RC.

## To Be Optimized Items

* SQL will be parsed twice by Apache ShardingSphere and Seata.
