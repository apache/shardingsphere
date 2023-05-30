+++
title = "REFRESH DATABASE METADATA FROM GOVERNANCE CENTER"
weight = 9
+++

### 描述

`REFRESH DATABASE METADATA FROM GOVERNANCE CENTER` 语法用于从治理中心拉取最新配置，刷新本地逻辑库元数据。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
RefreshDatabaseMetadataFromGovernanceCenter ::=
  'REFRESH' 'DATABASE' 'METADATA' databaseName? 'FROM' 'GOVERNANCE' 'CENTER'

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

- 刷新元数据需要使用 `DATABASE` 如果未使用 `DATABASE` 则会提示 `No database selected`

### 示例

- 刷新指定逻辑库的元数据

```sql
REFRESH DATABASE METADATA sharding_db FROM GOVERNANCE CENTER;
```

- 刷新所有逻辑库的元数据

```sql
REFRESH DATABASE METADATA FROM GOVERNANCE CENTER;
```

### 保留字

`REFRESH`、`DATABASE`、`METADATA`、`FROM`、`GOVERNANCE`、`CENTER`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)