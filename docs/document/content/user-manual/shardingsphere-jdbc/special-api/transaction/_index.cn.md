+++
title = "分布式事务"
weight = 3
chapter = true
+++

通过 Apache ShardingSphere 使用分布式事务，与本地事务并无区别。
Apache ShardingSphere 支持 LOCAL、XA 和 BASE 事务，事务类型由事务规则中的 `defaultType` 确定。事务开始后，其类型在提交或回滚前保持不变。
