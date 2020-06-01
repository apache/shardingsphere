+++
title = "Atomikos 事务"
weight = 4
+++

Apache ShardingSphere 默认的 XA 事务管理器为 Atomikos。

## 数据恢复

在项目的 `logs` 目录中会生成`xa_tx.log`, 这是 XA 崩溃恢复时所需的日志，请勿删除。

## 修改配置

可以通过在项目的 classpath 中添加 `jta.properties` 来定制化 Atomikos 配置项。

详情请参见[Atomikos官方文档](https://www.atomikos.com/Documentation/JtaProperties)。
