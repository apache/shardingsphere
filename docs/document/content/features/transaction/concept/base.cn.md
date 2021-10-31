+++
title = "柔性事务"
weight = 3
+++

柔性事务在 2008 年发表的一篇[论文](https://queue.acm.org/detail.cfm?id=1394128)中被最早提到，
它提倡采用最终一致性放宽对强一致性的要求，以达到事务处理并发度的提升。

TCC 和 Sage 是两种常见实现方案。
他们主张开发者自行实现对数据库的反向操作，来达到数据在回滚时仍能够保证最终一致性。
[SEATA](https://github.com/seata/seata) 实现了 SQL 反向操作的自动生成，可以使柔性事务不再必须由开发者介入才能使用。

Apache ShardingSphere 集成了 SEATA 作为柔性事务的使用方案。
