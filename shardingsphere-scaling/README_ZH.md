# ShardingScaling - ShardingSphere弹性伸缩组件

## 概述

下图可以清晰地表明这个组件的作用：

![scale out](https://user-images.githubusercontent.com/14773179/75600294-8516d500-5ae8-11ea-9635-5656b72242e3.png)

关于图片的补充说明：

1. 只支持迁移在分片规则中配置的表，暂不支持迁移指定的表。
2. 迁移过程分为两步——历史数据迁移和实时数据迁移。
  - 在历史数据迁移过程中，ShardingSphere-Scaling 使用 `select *`语句去获取数据，使用 `insert` 语句将数据迁移到目标库中；
  - 在实时数据迁移过程中，ShardingSphere-Scaling 使用 binlog 来迁移数据，并且在迁移前会标记下 binlog 位置。

3. 如果源 schema 中的表有主键，ShardingSphere-Scaling 就能够使用 `where` 声明的条件，并发地迁移表中的数据。

## 环境准备

MySQL：5.1.15 ~ 5.7.x

ShardingSphere-Proxy：3.x ~ 5.x

## 如何运行

参考[快速入门](https://shardingsphere.apache.org/document/current/cn/quick-start/shardingsphere-scaling-quick-start/)

## 更多文档

[概览](https://shardingsphere.apache.org/document/current/cn/features/shardingsphere-scaling/)

[实现原理](https://shardingsphere.apache.org/document/current/cn/features/shardingsphere-scaling/principle/)

