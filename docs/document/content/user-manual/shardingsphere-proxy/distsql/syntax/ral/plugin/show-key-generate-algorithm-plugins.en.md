+++
title = "SHOW KEY GENERATE ALGORITHM PLUGINS"
weight = 7
+++

### Description

The `"SHOW KEY GENERATE ALGORITHM PLUGINS"` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showKeyGenerateAlgorithmPlugins ::=
  'SHOW' 'KEY' 'GENERATE' 'ALGORITHM' 'PLUGINS'
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

- Query all the implementations for `org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm` interface

```sql
SHOW KEY GENERATE ALGORITHM PLUGINS
```

```sql
SHOW KEY GENERATE ALGORITHM PLUGINS;
+-----------+--------------+-------------+
| type      | type_aliases | description |
+-----------+--------------+-------------+
| UUID      |              |             |
| SNOWFLAKE |              |             |
+-----------+--------------+-------------+
2 rows in set (0.05 sec)
```

### Reserved word

`SHOW`, `KEY`, `GENERATE`, `ALGORITHM`, `PLUGINS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
