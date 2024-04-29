+++
title = "IMPORT DATABASE CONFIGURATION"
weight = 14
+++

### 描述

`IMPORT DATABASE CONFIGURATION` 语法用于从 `YAML` 中的配置导入逻辑库。

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

- 当元数据中已存在同名逻辑库时，无法导入；
- 当 YAML 中 `databaseName` 为空时，无法导入；
- 当 YAML 中 `dataSources` 为空时，只导入空的逻辑库。

### 示例

```sql
IMPORT DATABASE CONFIGURATION FROM FILE "/xxx/config_sharding_db.yaml";
```

### 保留字

`IMPORT`、`DATABASE`、`CONFIGURATION`、`FROM`、`FILE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
