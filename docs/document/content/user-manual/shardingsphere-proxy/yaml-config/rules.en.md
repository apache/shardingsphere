+++
title = "Rules"
weight = 3
+++

## Background

This section explains how to configure the ShardingSphere-Proxy rules.

## Parameters Explained

Rules configuration for ShardingSphere-Proxy is the same as ShardingSphere-JDBC.
For details, please refer to [ShardingSphere-JDBC Rules Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/).

## Notice

Unlike ShardingSphere-JDBC, the following rules need to be configured in ShardingSphere-Proxy's `global.yaml`:

* [SQL Parsing](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-parser/)
```yaml
sqlParser:
  sqlStatementCache:
    initialCapacity: 2000
    maximumSize: 65535
  parseTreeCache:
    initialCapacity: 128
    maximumSize: 1024
```
* [Distributed Transaction](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction/)
```yaml
transaction:
  defaultType: XA
  providerType: Atomikos
```
* [SQL Translator](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-translator/)
```yaml
sqlTranslator:
  type:
  useOriginalSQLWhenTranslatingFailed:
```
* [SQL Federation](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-federation/)
```yaml
sqlFederation:
  sqlFederationEnabled: true
  allQueryUseSQLFederation: false
  executionPlanCache:
    initialCapacity: 2000
    maximumSize: 65535
```
