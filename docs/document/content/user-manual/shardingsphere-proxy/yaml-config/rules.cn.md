+++
title = "规则配置"
weight = 3
+++

## 背景信息

本节介绍如何进行 ShardingSphere-Proxy 的规则配置。

## 参数解释

ShardingSphere-Proxy 的规则配置与 ShardingSphere-JDBC 一致，具体规则请参考 [ShardingSphere-JDBC 规则配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/)。

## 注意事项

与 ShardingSphere-JDBC 不同的是，以下规则需要配置在 ShardingSphere-Proxy 的 `global.yaml` 中：

* [SQL 解析](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
```yaml
sqlParser:
  sqlStatementCache:
    initialCapacity: 2000
    maximumSize: 65535
  parseTreeCache:
    initialCapacity: 128
    maximumSize: 1024
```
* [分布式事务](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction/)
```yaml
transaction:
  defaultType: XA
  providerType: Atomikos
```
* [SQL 翻译](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-translator/)
```yaml
sqlTranslator:
  type:
  useOriginalSQLWhenTranslatingFailed:
```
* [联邦查询](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-federation/)
```yaml
sqlFederation:
  sqlFederationEnabled: true
  allQueryUseSQLFederation: false
  executionPlanCache:
    initialCapacity: 2000
    maximumSize: 65535
```
