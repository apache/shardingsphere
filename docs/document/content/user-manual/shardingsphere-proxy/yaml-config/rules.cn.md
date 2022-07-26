+++
title = "规则配置"
weight = 3
+++

## 背景信息

本节介绍如何进行 ShardingSphere-Proxy 的规则配置。

## 参数解释

ShardingSphere-Proxy 的规则配置与 ShardingSphere-JDBC 一致，具体规则请参考 [ShardingSphere-JDBC 规则配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/)。

## 注意事项

与 ShardingSphere-JDBC 不同的是，以下规则需要配置在 ShardingSphere-Proxy 的 `server.yaml` 中：

* [SQL 解析](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
* [分布式事务](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction/)
* [SQL 翻译](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-translator/)
