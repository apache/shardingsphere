+++
title = "BASE"
weight = 3
+++

A [paper](https://queue.acm.org/detail.cfm?id=1394128) published in 2008 first mentioned on BASE transaction,
it advocates the use of eventual consistency to instead of consistency when improve concurrency of transaction processing.

TCC and Saga are two regular implementations.
They use reverse operation implemented by developers themselves to ensure the eventual consistency when data rollback.
[SEATA](https://github.com/seata/seata) implements SQL reverse operation automatically, 
so that BASE transaction can be used without the intervention of developers.

Apache ShardingSphere integrates SEATA as solution of BASE transaction.
