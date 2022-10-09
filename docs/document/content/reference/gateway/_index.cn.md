+++
pre = "<b>7.2. </b>"
title = "数据库网关"
weight = 2
+++

Apache ShardingSphere 提供了 SQL 方言翻译的能力，能否实现数据库方言之间的自动转换。例如，用户可以使用 MySQL 客户端连接 ShardingSphere 并发送基于 MySQL 方言的 SQL，ShardingSphere 能自动识别用户协议与存储节点类型，自动完成 SQL 方言转换，访问 PostgreSQL 等异构存储节点。

![网关](https://shardingsphere.apache.org/document/current/img/gateway/gateway_cn.png)


