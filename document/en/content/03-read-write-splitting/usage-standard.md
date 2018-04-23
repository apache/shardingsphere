+++
toc = true
title = "Usage standard"
weight = 2
+++

## Supported List

1. To configure Read-write splitting on single master and multiple slaves. You can use this function independently or along with sharding.
1. Provide SQL passthrough when the Read-write splitting is used independently .
1. In order to ensure data consistency, if there is a write operation, the later read operations in the same thread and the same connection are executed in the Master.
1. Mandatory Master rounting strategy based in SQL Hint.

## Unsupported List

1. Data synchronization between Master and Slaves.
1. Inconsistency of data between Master and Slaves due to transmission delay.
1. Multiple Masters to provide writing operations.
