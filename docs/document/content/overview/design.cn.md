+++
pre = "<b>1.2 </b>"
title = "设计哲学"
weight = 2
chapter = true
+++

ShardingSphere 采用了 Database Plus 设计哲学，该理念致力于构建数据库上层的标准和生态，在生态中补充数据库所缺失的能力。

Apache ShardingSphere 的可插拔架构划分为 3 层，它们是：L1 内核层、L2 功能层、L3 生态层。

![ShardingSphere Architecture](https://shardingsphere.apache.org/document/current/img/overview.cn_v2.png)

## L1 内核层

是数据库基本能力的抽象，其所有组件均必须存在，但具体实现方式可通过可插拔的方式更换。 主要包括查询优化器、分布式事务引擎、分布式执行引擎、权限引擎和调度引擎等。

## L2 功能层

用于提供增量能力，其所有组件均是可选的，可以包含零至多个组件。组件之间完全隔离，互无感知，多组件可通过叠加的方式相互配合使用。 主要包括数据分片、读写分离、数据库高可用、数据加密、影子库等。用户自定义功能可完全面向 Apache ShardingSphere 定义的顶层接口进行定制化扩展，而无需改动内核代码。

## L3 生态层

用于对接和融入现有数据库生态，包括数据库协议、SQL 解析器和存储适配器，分别对应于 Apache ShardingSphere 以数据库协议提供服务的方式、SQL 方言操作数据的方式以及对接存储节点的数据库类型。