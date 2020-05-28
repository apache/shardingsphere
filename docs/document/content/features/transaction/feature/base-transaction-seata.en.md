+++
pre = "<b>3.4.2.3 </b>"
title = "Seata BASE transaction"
weight = 3
+++

## Features

* Support RC isolation level.
* Rollback transaction according to undo log.
* Support recovery committing transaction automatically after the service is down.

## Dependency

* Need to deploy seata-server process to do transaction coordination.

## Need to Optimize

* SQL will be parsed twice by Apache ShardingSphere and Seata.
