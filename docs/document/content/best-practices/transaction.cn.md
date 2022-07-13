+++
pre = "<b>7.2. </b>"
title = "分布式事务"
weight = 2
chapter = true
+++

## 适用场景

Apache ShardingSphere 提供分布式场景下的事务语义，当有事务需求时，可以使用。ShardingSphere 提供了三种事务模式：LOCAL，XA，BASE，以应对不同的场景。

- LOCAL：适用于对数据一致性要求不高的场景。
- XA：提供了原子性的保证，保证了数据不丢，不保证快照读。适用于对一致性要求相对高，没有快照读要求的场景。在对一致性要求较高的场景，较好的选择是使用 XA 的 Narayana 实现。
- BASE：在一致性和性能之间做了权衡，具体参考 Seata 官网。

## 操作步骤

参考 [ShardingSphere 使用 XA Narayana](/cn/user-manual/shardingsphere-jdbc/special-api/transaction/narayana/)