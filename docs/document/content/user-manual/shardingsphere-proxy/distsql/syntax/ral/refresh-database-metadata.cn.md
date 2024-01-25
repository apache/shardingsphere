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

- 刷新元数据使用 `FORCE` 时，将会从本地获取最新元数据，并写入到治理中心，未开启 `FORCE` 则会从治理中心拉取最新配置

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
