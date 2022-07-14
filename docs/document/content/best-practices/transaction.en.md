+++
pre = "<b>7.2. </b>"
title = "Distributed Transaction"
weight = 2
chapter = true
+++

## Scenarios

Apache ShardingSphere provides transaction semantics in distributed scenarios that can be used when transactional are required.
ShardingSphere provides three transaction modes: LOCAL, XA, and BASE to address different scenarios.

- LOCAL: Suitable for scenarios where data consistency requirements are not high.
- XA: Provides atomicity guarantees, ensures that data is not lost, and snapshot reads are not guaranteed. Suitable for scenarios where consistency requirements are relatively high and there is no snapshot read requirement. In scenarios where consistency is required, a better choice is a Narayana implementation using XA.
- BASE: A trade-off was made between consistency and performance, for details see Seata website.

## Procedure

See [ShardingSphere Using XA Narayana](/en/user-manual/shardingsphere-jdbc/special-api/transaction/narayana/)