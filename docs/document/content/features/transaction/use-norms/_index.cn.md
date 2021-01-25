+++
pre = "<b>3.2.3. </b>"
title = "使用规范"
weight = 3
chapter = true
+++

## 背景

虽然 Apache ShardingSphere 希望能够完全兼容所有的分布式事务场景，并在性能上达到最优，但在 CAP 定理所指导下，分布式事务必然有所取舍。
Apache ShardingSphere 希望能够将分布式事务的选择权交给使用者，在不同的场景用使用最适合的分布式事务解决方案。
