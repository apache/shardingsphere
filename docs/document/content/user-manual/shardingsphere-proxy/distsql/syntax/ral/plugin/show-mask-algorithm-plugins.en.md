+++
title = "SHOW MASK ALGORITHM PLUGINS"
weight = 5
+++

### Description

The `SHOW MASK ALGORITHM PLUGINS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.mask.spi.MaskAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showMaskAlgorithmPlugins ::=
  'SHOW' 'MASK' 'ALGORITHM' 'PLUGINS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns      | Description  |
|--------------|--------------|
| type         | type         |
| type_aliases | type aliases |
| description  | description  |

### Example

- Query all the implementations for `org.apache.shardingsphere.mask.spi.MaskAlgorithm` interface

```sql
SHOW MASK ALGORITHM PLUGINS
```

```sql
SHOW MASK ALGORITHM PLUGINS;
+------------------------------+--------------+-------------+
| type                         | type_aliases | description |
+------------------------------+--------------+-------------+
| MD5                          |              |             |
| KEEP_FIRST_N_LAST_M          |              |             |
| KEEP_FROM_X_TO_Y             |              |             |
| MASK_AFTER_SPECIAL_CHARS     |              |             |
| MASK_BEFORE_SPECIAL_CHARS    |              |             |
| MASK_FIRST_N_LAST_M          |              |             |
| MASK_FROM_X_TO_Y             |              |             |
| GENERIC_TABLE_RANDOM_REPLACE |              |             |
+------------------------------+--------------+-------------+
8 rows in set (0.13 sec)
```

### Reserved word

`SHOW`, `MASK`, `ALGORITHM`, `PLUGINS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
