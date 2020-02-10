+++
pre = "<b>3.4.2.4 </b>"
toc = true
title = "Seata BASE transaction"
weight = 4
+++

## Function

* Fully support cross-database transactions.
* Support RC isolation.
* Rollback transaction according to undo snapshot.
* Support recovery committing/rolling back transaction automatically after the service is down.

## Prerequisite

* Need to deploy seata-server process to do transaction coordination.

## Need to Optimize

* SQL will be parsed twice by ShardingSphere and Seata.



