+++
title = "IMPORT METADATA"
weight = 19
+++

### Description

The `IMPORT METADATA` syntax is used to import cluster metadata from file or inline value.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ImportMetaData ::=
  'IMPORT' 'METADATA' (metaDataValue | 'FROM' 'FILE' filePath)

metaDataValue ::=
  string

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `metaDataValue` should be a Base64 string exported by `EXPORT METADATA`;
- `FROM FILE` should point to a JSON file exported by `EXPORT METADATA TO FILE`;
- If the imported metadata contains an existing logical database, import will fail;
- If the metadata file does not exist or content format is invalid, import will fail.

### Example

```sql
IMPORT METADATA FROM FILE '/tmp/metadata.json';
```

```sql
IMPORT METADATA 'eyJtZXRhX2RhdGEiOns...';
```

### Reserved word

`IMPORT`, `METADATA`, `FROM`, `FILE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
