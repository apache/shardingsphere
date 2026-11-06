+++
title = "REFRESH DATABASE METADATA"
weight = 10
+++

### 描述

`REFRESH DATABASE METADATA` 语法用于刷新本地逻辑库元数据。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
RefreshDatabaseMetadata ::=
  'FORCE'? 'REFRESH' 'DATABASE' 'METADATA' databaseName?

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认刷新所有逻辑库的元数据

- 刷新会加载最新的物理表元数据，并在发布前校验所有相关物理表，不受 `check-table-metadata-enabled` 配置影响。
  结构不一致或部分物理表缺失时，刷新会在发布前失败。
  可选的 `FORCE` 关键字不改变这一行为。

- 各节点应用表结构变更或删除后，基于该表旧结构 prepare 的语句需要重新 prepare，否则新执行会报错并提示 `Table metadata changed; prepare the statement again.`。
  ShardingSphere-JDBC 和 MySQL 协议暴露错误码 `10025`、SQLState `HY000`；PostgreSQL 和 openGauss 通过既有协议映射返回 SQLState `58000`。
  结构未变或不相关表的旧绑定仍可使用，已经创建的结果集仍可读取。

### 示例

- 刷新指定逻辑库的元数据

```sql
REFRESH DATABASE METADATA sharding_db;
```

- 刷新所有逻辑库的元数据

```sql
REFRESH DATABASE METADATA;
```

- 强制刷新所有逻辑库的元数据

```sql
FORCE REFRESH DATABASE METADATA;
```

### 保留字

`FORCE`、`REFRESH`、`DATABASE`、`METADATA`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
