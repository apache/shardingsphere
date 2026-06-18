+++
title = "EXPORT METADATA"
weight = 18
+++

### 描述

`EXPORT METADATA` 语法用于导出集群元数据，导出格式为 `JSON`。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ExportMetaData ::=
  'EXPORT' 'METADATA' ('TO' 'FILE' filePath)?

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列名         | 说明                                   |
|--------------|----------------------------------------|
| id           | 当前计算节点 id                        |
| create_time  | 导出时间                               |
| cluster_info | 导出的元数据（或指定 `TO FILE` 时的成功提示） |

### 补充说明

- 未指定 `filePath` 时，`cluster_info` 列返回 Base64 编码的元数据内容；
- 指定 `filePath` 时，会将元数据写入文件，`cluster_info` 列返回成功提示；
- 若 `filePath` 的父目录不存在会自动创建，若文件已存在会被覆盖。

### 示例

```sql
EXPORT METADATA;
```

```sql
EXPORT METADATA TO FILE '/tmp/metadata.json';
```

### 保留字

`EXPORT`、`METADATA`、`TO`、`FILE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
