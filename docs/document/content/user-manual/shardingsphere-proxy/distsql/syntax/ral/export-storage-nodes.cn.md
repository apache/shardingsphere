+++
title = "EXPORT STORAGE NODES"
weight = 20
+++

### 描述

`EXPORT STORAGE NODES` 语法用于导出存储节点配置，导出格式为 `JSON`。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ExportStorageNodes ::=
  'EXPORT' 'STORAGE' 'NODES' ('FROM' databaseName)? ('TO' 'FILE' filePath)?

databaseName ::=
  identifier

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列名          | 说明                                      |
|---------------|-------------------------------------------|
| id            | 当前计算节点 id                           |
| create_time   | 导出时间                                  |
| storage_nodes | 导出的存储节点 JSON（或指定 `TO FILE` 时的成功提示） |

### 补充说明

- 未指定 `databaseName` 时，导出所有逻辑库的存储节点；
- 指定 `databaseName` 时，仅导出指定逻辑库的存储节点；
- 指定的 `databaseName` 不存在时，导出会失败；
- 指定 `filePath` 时，会将结果写入文件，若文件已存在会被覆盖；
- 导出内容包含 `username`、`password` 等连接信息，请注意保护导出结果。

### 示例

```sql
EXPORT STORAGE NODES;
```

```sql
EXPORT STORAGE NODES FROM sharding_db TO FILE '/tmp/storage-nodes.json';
```

### 保留字

`EXPORT`、`STORAGE`、`NODES`、`FROM`、`TO`、`FILE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
