+++
title = "EXPORT STORAGE NODES"
weight = 20
+++

### Description

The `EXPORT STORAGE NODES` syntax is used to export storage node configurations in `JSON` format.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ExportStorageNodes ::=
  'EXPORT' 'STORAGE' 'NODES' ('FROM' databaseName)? ('TO' 'FILE' filePath)?

databaseName ::=
  identifier

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns       | Description                                                   |
|---------------|---------------------------------------------------------------|
| id            | current compute node id                                       |
| create_time   | export time                                                   |
| storage_nodes | exported storage node JSON (or success message when `TO FILE` is set) |

### Supplement

- When `databaseName` is not specified, storage nodes of all logical databases are exported;
- When `databaseName` is specified, only storage nodes of the specified logical database are exported;
- If the specified `databaseName` does not exist, export will fail;
- When `filePath` is specified, the result is written to file and existing file content will be overwritten;
- Exported JSON contains connection information such as `username` and `password`; protect the output properly.

### Example

```sql
EXPORT STORAGE NODES;
```

```sql
EXPORT STORAGE NODES FROM sharding_db TO FILE '/tmp/storage-nodes.json';
```

### Reserved word

`EXPORT`, `STORAGE`, `NODES`, `FROM`, `TO`, `FILE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
