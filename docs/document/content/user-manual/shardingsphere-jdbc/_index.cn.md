+++
pre = "<b>4.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
chapter = true
+++

配置是 ShardingSphere-JDBC 中唯一与应用开发者交互的模块，通过它可以快速清晰的理解 ShardingSphere-JDBC 所提供的功能。

本章节是 ShardingSphere-JDBC 的配置参考手册，需要时可当做字典查阅。

ShardingSphere-JDBC 提供了 2 种配置方式，用于不同的使用场景。
通过配置，应用开发者可以灵活的使用数据分片、读写分离、数据加密、影子库等功能，并且能够叠加使用。

混合规则配置与单一规则配置一脉相承，只是从配置单一的规则项到配置多个规则项的异同。

需要注意的是，规则项之间的叠加使用是通过数据源名称和表名称关联的。
如果前一个规则是面向数据源聚合的，下一个规则在配置数据源时，则需要使用前一个规则配置的聚合后的逻辑数据源名称；
同理，如果前一个规则是面向表聚合的，下一个规则在配置表时，则需要使用前一个规则配置的聚合后的逻辑表名称。

更多使用细节请参见[使用示例](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example-generator)。

> **注意**：使用 ShardingSphere-JDBC 接入端时，需特别关注应用的内存配置。由于 Antlr 在 SQL 解析过程中，会使用内部缓存来提升性能，如果应用的 SQL 模板数量过多，则会导致缓存不断增长，占用大量堆内存。
根据 ANTLR 官方 [issue#4232](https://github.com/antlr/antlr4/issues/4232) 的反馈，目前该问题尚未得到优化，应用接入 ShardingSphere-JDBC 时，建议通过 `-Xmx` 参数设置合理的堆内存大小，避免因内存不足导致的 OOM。
