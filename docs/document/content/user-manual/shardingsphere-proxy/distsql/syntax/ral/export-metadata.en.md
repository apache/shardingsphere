+++
title = "EXPORT METADATA"
weight = 18
+++

### Description

The `EXPORT METADATA` syntax is used to export cluster metadata in `JSON` format.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ExportMetaData ::=
  'EXPORT' 'METADATA' ('TO' 'FILE' filePath)?

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns      | Description                                                  |
|--------------|--------------------------------------------------------------|
| id           | current compute node id                                      |
| create_time  | export time                                                  |
| cluster_info | exported metadata (or success message when `TO FILE` is set) |

### Supplement

- When `filePath` is not specified, the `cluster_info` column returns Base64-encoded metadata;
- When `filePath` is specified, metadata is written to file and `cluster_info` returns a success message;
- The parent directory of `filePath` will be created automatically if needed, and existing file content will be overwritten.

### Example

```sql
EXPORT METADATA;
```

```sql
EXPORT METADATA TO FILE '/tmp/metadata.json';
```

### Reserved word

`EXPORT`, `METADATA`, `TO`, `FILE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
