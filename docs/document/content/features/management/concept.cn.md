+++
title = "核心概念"
weight = 1
+++

## 熔断

阻断 Apache ShardingSphere 和数据库的连接。
当某个 Apache ShardingSphere 节点超过负载后，停止该节点对数据库的访问，使数据库能够保证足够的资源为其他节点提供服务。

## 限流

面对超负荷的请求开启限流，以保护部分请求可以得以高质量的响应。
