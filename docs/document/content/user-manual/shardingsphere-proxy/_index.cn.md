+++
pre = "<b>4.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
chapter = true
+++

配置是 ShardingSphere-Proxy 中唯一与开发者交互的模块，通过它可以快速清晰的理解 ShardingSphere-Proxy 所提供的功能。

本章节是 ShardingSphere-Proxy 的配置参考手册，需要时可当做字典查阅。

ShardingSphere-Proxy 提供基于 YAML 的配置方式，并使用 DistSQL 进行交互。
通过配置，应用开发者可以灵活的使用数据分片、读写分离、数据加密、影子库等功能，并且能够叠加使用。

规则配置部分与 ShardingSphere-JDBC 的 YAML 配置完全一致。
DistSQL 与 YAML 配置能够相互取代。

更多使用细节请参见[使用示例](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-proxy-example)。
