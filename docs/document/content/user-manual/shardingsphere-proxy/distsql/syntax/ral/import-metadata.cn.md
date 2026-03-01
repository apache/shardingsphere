+++
title = "IMPORT METADATA"
weight = 19
+++

### 描述

`IMPORT METADATA` 语法用于从文件或内联值导入集群元数据。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ImportMetaData ::=
  'IMPORT' 'METADATA' (metaDataValue | 'FROM' 'FILE' filePath)

metaDataValue ::=
  string

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `metaDataValue` 需要是通过 `EXPORT METADATA` 导出的 Base64 字符串；
- `FROM FILE` 需要指向通过 `EXPORT METADATA TO FILE` 导出的 JSON 文件；
- 如果导入元数据中包含已存在的逻辑库，导入会失败；
- 如果文件不存在或内容格式不正确，导入会失败。

### 示例

```sql
IMPORT METADATA FROM FILE '/tmp/metadata.json';
```

```sql
IMPORT METADATA 'eyJtZXRhX2RhdGEiOns...';
```

### 保留字

`IMPORT`、`METADATA`、`FROM`、`FILE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
