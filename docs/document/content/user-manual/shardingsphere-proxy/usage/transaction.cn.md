+++
title = "分布式事务"
weight = 3
+++

ShardingSphere-Proxy 接入的分布式事务 API 同 ShardingSphere-JDBC 保持一致，支持 LOCAL，XA，BASE 类型的事务。

## XA 事务

ShardingSphere-Proxy 原生支持 XA 事务，默认的事务管理器为 Atomikos。
可以通过在 ShardingSphere-Proxy 的 conf 目录中添加 `jta.properties` 来定制化 Atomikos 配置项。
具体的配置规则请参考 Atomikos 的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。

## BASE事务

BASE 目前没有集成至 ShardingSphere-Proxy 的二进制发布包中，使用时需要将实现了 `ShardingTransactionManager` SPI 的 jar 拷贝至 conf/lib 目录，然后切换事务类型为 BASE。
