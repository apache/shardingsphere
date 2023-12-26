+++
title = "IMPORT DATABASE CONFIGURATION"
weight = 13
+++

### 描述

`IMPORT DATABASE CONFIGURATION` 语法用于将 `YAML` 中的配置导入到指定逻辑库中。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ExportDatabaseConfiguration ::=
  'IMPORT' 'DATABASE' 'CONFIGURATION' 'FROM' 'FILE' filePath

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE` 如果也未使用 `DATABASE` 则会提示 `No database selected`。

- `IMPORT DATABASE CONFIGURATION` 语法仅支持对空逻辑库进行导入操作。

### 示例

- 将 `YAML` 中的配置导入到指定逻辑库中

```sql
IMPORT DATABASE CONFIGURATION FROM FILE "/xxx/config_sharding_db.yaml";
```

- 将 `YAML` 中的配置导入到当前逻辑库中

```sql
IMPORT DATABASE CONFIGURATION FROM FILE "/xxx/config_sharding_db.yaml";
```

### 保留字

`IMPORT`、`DATABASE`、`CONFIGURATION`、`FROM`、`FILE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
